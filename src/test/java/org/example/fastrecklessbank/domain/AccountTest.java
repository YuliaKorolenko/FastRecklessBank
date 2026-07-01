package org.example.fastrecklessbank.domain;

import org.example.fastrecklessbank.common.exception.BalanceLimitExceededException;
import org.example.fastrecklessbank.common.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private Account account(long balanceCents) {
        return new Account(new AccountId(UUID.randomUUID()), "Alice", "Smith", Money.ofCents(balanceCents));
    }

    @Test
    void depositIncreasesBalance() {
        Account a = account(100);
        a.deposit(Money.ofCents(50));
        assertThat(a.balance()).isEqualTo(Money.ofCents(150));
    }

    @Test
    void withdrawDecreasesBalance() {
        Account a = account(100);
        a.withdraw(Money.ofCents(40));
        assertThat(a.balance()).isEqualTo(Money.ofCents(60));
    }

    @Test
    void withdrawBeyondBalanceThrowsAndLeavesBalanceUnchanged() {
        Account a = account(100);
        assertThatThrownBy(() -> a.withdraw(Money.ofCents(101)))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(a.balance()).isEqualTo(Money.ofCents(100));
    }

    @Test
    void balanceMayReachButNotExceedTheLimit() {
        // opening exactly at the limit is allowed
        Account atLimit = account(Money.MAX_CENTS);
        assertThat(atLimit.balance().cents()).isEqualTo(Money.MAX_CENTS);

        // opening above the limit is rejected
        assertThatThrownBy(() -> account(Money.MAX_CENTS + 1))
                .isInstanceOf(BalanceLimitExceededException.class);
    }

    @Test
    void depositThatWouldExceedLimitThrowsAndLeavesBalanceUnchanged() {
        Account a = account(Money.MAX_CENTS - 10);
        assertThatThrownBy(() -> a.deposit(Money.ofCents(11)))
                .isInstanceOf(BalanceLimitExceededException.class);
        assertThat(a.balance().cents()).isEqualTo(Money.MAX_CENTS - 10);

        // depositing right up to the limit still works
        a.deposit(Money.ofCents(10));
        assertThat(a.balance().cents()).isEqualTo(Money.MAX_CENTS);
    }
}
