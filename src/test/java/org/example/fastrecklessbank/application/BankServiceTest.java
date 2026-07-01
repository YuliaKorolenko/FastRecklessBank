package org.example.fastrecklessbank.application;

import org.example.fastrecklessbank.common.exception.AccountNotFoundException;
import org.example.fastrecklessbank.common.exception.BalanceLimitExceededException;
import org.example.fastrecklessbank.common.exception.InsufficientFundsException;
import org.example.fastrecklessbank.common.exception.InvalidAmountException;
import org.example.fastrecklessbank.domain.Account;
import org.example.fastrecklessbank.domain.AccountId;
import org.example.fastrecklessbank.domain.Money;
import org.example.fastrecklessbank.infrastructure.InMemoryAccountRepository;
import org.example.fastrecklessbank.infrastructure.InMemoryTransferHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankServiceTest {

    private BankService service;

    @BeforeEach
    void setUp() {
        service = new BankService(new InMemoryAccountRepository(), new InMemoryTransferHistoryRepository());
    }

    @Test
    void createsAccountWithInitialDeposit() {
        Account account = service.createAccount("Alice", "Smith", 10_000);
        assertThat(account.balance().cents()).isEqualTo(10_000);
        assertThat(account.name()).isEqualTo("Alice");
        assertThat(account.surname()).isEqualTo("Smith");
        assertThat(service.listAccounts()).contains(account);
    }

    @Test
    void depositAndWithdraw() {
        Account account = service.createAccount("Alice", "Smith", 0);
        service.deposit(account.id(), 500);
        assertThat(service.requireAccount(account.id()).balance().cents()).isEqualTo(500);
        service.withdraw(account.id(), 200);
        assertThat(service.requireAccount(account.id()).balance().cents()).isEqualTo(300);
    }

    @Test
    void withdrawBeyondBalanceThrows() {
        Account account = service.createAccount("Alice", "Smith", 100);
        assertThatThrownBy(() -> service.withdraw(account.id(), 101))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(service.requireAccount(account.id()).balance().cents()).isEqualTo(100);
    }

    @Test
    void invalidAmountsRejected() {
        Account account = service.createAccount("Alice", "Smith", 100);
        assertThatThrownBy(() -> service.deposit(account.id(), 0)).isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> service.deposit(account.id(), -10)).isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void unknownAccountThrows() {
        assertThatThrownBy(() -> service.deposit(new AccountId(UUID.randomUUID()), 100))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transferMovesMoneyAndRecordsHistory() {
        Account from = service.createAccount("Alice", "Smith", 1_000);
        Account to = service.createAccount("Bob", "Jones", 0);

        service.transfer(from.id(), to.id(), 400);

        assertThat(service.requireAccount(from.id()).balance().cents()).isEqualTo(600);
        assertThat(service.requireAccount(to.id()).balance().cents()).isEqualTo(400);
        assertThat(service.getRecentTransfers(from.id())).hasSize(1);
        assertThat(service.getRecentTransfers(from.id()).getFirst().amount().cents()).isEqualTo(400);
        // incoming transfer is not recorded as outgoing for the recipient
        assertThat(service.getRecentTransfers(to.id())).isEmpty();
    }

    @Test
    void transferToSameAccountRejected() {
        Account account = service.createAccount("Alice", "Smith", 1_000);
        assertThatThrownBy(() -> service.transfer(account.id(), account.id(), 100))
                .isInstanceOf(InvalidAmountException.class);
        assertThat(service.requireAccount(account.id()).balance().cents()).isEqualTo(1_000);
    }

    @Test
    void depositExceedingLimitRejected() {
        Account account = service.createAccount("Alice", "Smith", Money.MAX_CENTS);
        assertThatThrownBy(() -> service.deposit(account.id(), 1))
                .isInstanceOf(BalanceLimitExceededException.class);
        assertThat(service.requireAccount(account.id()).balance().cents()).isEqualTo(Money.MAX_CENTS);
    }

    @Test
    void transferExceedingRecipientLimitLeavesBothBalancesUnchanged() {
        Account from = service.createAccount("Alice", "Smith", 1_000);
        Account to = service.createAccount("Bob", "Jones", Money.MAX_CENTS);

        assertThatThrownBy(() -> service.transfer(from.id(), to.id(), 500))
                .isInstanceOf(BalanceLimitExceededException.class);

        // atomic: neither side changed, and nothing was recorded in history
        assertThat(service.requireAccount(from.id()).balance().cents()).isEqualTo(1_000);
        assertThat(service.requireAccount(to.id()).balance().cents()).isEqualTo(Money.MAX_CENTS);
        assertThat(service.getRecentTransfers(from.id())).isEmpty();
    }

    @Test
    void insufficientFundsTransferLeavesBothBalancesUnchanged() {
        Account from = service.createAccount("Alice", "Smith", 100);
        Account to = service.createAccount("Bob", "Jones", 50);

        assertThatThrownBy(() -> service.transfer(from.id(), to.id(), 500))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(service.requireAccount(from.id()).balance().cents()).isEqualTo(100);
        assertThat(service.requireAccount(to.id()).balance().cents()).isEqualTo(50);
        assertThat(service.getRecentTransfers(from.id())).isEmpty();
    }

    @Test
    void concurrentTransfersConserveTotalBalance() throws InterruptedException {
        Account a = service.createAccount("A", "A", 1_000_000);
        Account b = service.createAccount("B", "B", 1_000_000);
        long total = 2_000_000;

        int threads = 16;
        int perThread = 500;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            // half the threads push a->b, the other half b->a (opposing directions)
            boolean forward = t % 2 == 0;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        try {
                            if (forward) {
                                service.transfer(a.id(), b.id(), 1);
                            } else {
                                service.transfer(b.id(), a.id(), 1);
                            }
                        } catch (InsufficientFundsException ignored) {
                            // acceptable under contention; balance must still be conserved
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        // if a deadlock occurred this would time out and fail
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        long finalTotal = service.requireAccount(a.id()).balance().cents()
                + service.requireAccount(b.id()).balance().cents();
        assertThat(finalTotal).isEqualTo(total);
    }

    @Test
    void concurrentDepositsAreNotLost() throws InterruptedException {
        Account account = service.createAccount("Alice", "Smith", 0);
        int threads = 16;
        int perThread = 1_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger failures = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        service.deposit(account.id(), 1);
                    }
                } catch (RuntimeException e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();
        assertThat(failures.get()).isZero();
        assertThat(service.requireAccount(account.id()).balance().cents())
                .isEqualTo((long) threads * perThread);
    }
}
