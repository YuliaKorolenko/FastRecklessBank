package org.example.fastrecklessbank.application;

import org.example.fastrecklessbank.application.port.AccountRepository;
import org.example.fastrecklessbank.application.port.TransferHistoryRepository;
import org.example.fastrecklessbank.common.exception.AccountNotFoundException;
import org.example.fastrecklessbank.common.exception.InvalidAmountException;
import org.example.fastrecklessbank.domain.Account;
import org.example.fastrecklessbank.domain.AccountId;
import org.example.fastrecklessbank.domain.Money;
import org.example.fastrecklessbank.domain.TransferRecord;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class BankService {

    private final AccountRepository accountRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    public BankService(AccountRepository accountRepository,
                       TransferHistoryRepository transferHistoryRepository) {
        this.accountRepository = accountRepository;
        this.transferHistoryRepository = transferHistoryRepository;
    }

    public Account createAccount(String name, String surname, long initialDepositCents) {
        if (name == null || name.isBlank()) {
            throw new InvalidAmountException("Account name must not be blank");
        }
        if (surname == null || surname.isBlank()) {
            throw new InvalidAmountException("Account surname must not be blank");
        }
        Account account = new Account(new AccountId(UUID.randomUUID()), name, surname,
                Money.ofCents(initialDepositCents));
        accountRepository.createAccount(account);
        return account;
    }

    public Account deposit(AccountId id, long amountCents) {
        Money amount = Money.ofPositiveCents(amountCents);
        Account account = requireAccount(id);
        account.lock();
        try {
            account.deposit(amount);
        } finally {
            account.unlock();
        }
        return account;
    }

    public Account withdraw(AccountId id, long amountCents) {
        Money amount = Money.ofPositiveCents(amountCents);
        Account account = requireAccount(id);
        account.lock();
        try {
            account.withdraw(amount);
        } finally {
            account.unlock();
        }
        return account;
    }

    /**
     * Atomically moves money from one account to another. Rejects self-transfers, locks both
     * accounts in {@link AccountId} order (deadlock-free), applies withdraw, deposit under the
     * locks, and records the outgoing transfer for the source account.
     */
    public Account transfer(AccountId fromId, AccountId toId, long amountCents) {
        if (fromId.equals(toId)) {
            throw new InvalidAmountException("Cannot transfer to the same account: " + fromId.id());
        }
        Money amount = Money.ofPositiveCents(amountCents);
        Account from = requireAccount(fromId);
        Account to = requireAccount(toId);

        Account first = isFirst(from, to) ? from : to;
        Account second = first == from ? to : from;

        first.lock();
        try {
            second.lock();
            try {
                from.withdraw(amount);
                to.deposit(amount);
            } finally {
                second.unlock();
            }
        } finally {
            first.unlock();
        }

        transferHistoryRepository.record(fromId, new TransferRecord(fromId, toId, amount));
        return from;
    }

    public Collection<Account> listAccounts() {
        return accountRepository.findAll();
    }

    public List<TransferRecord> getRecentTransfers(AccountId id) {
        requireAccount(id);
        return transferHistoryRepository.getRecentTransfers(id);
    }

    public Account requireAccount(AccountId id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id.id()));
    }

    private static boolean isFirst(Account a, Account b) {
        return a.id().compareTo(b.id()) <= 0;
    }
}
