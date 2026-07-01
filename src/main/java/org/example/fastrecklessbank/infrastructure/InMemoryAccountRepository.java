package org.example.fastrecklessbank.infrastructure;

import org.example.fastrecklessbank.application.port.AccountRepository;
import org.example.fastrecklessbank.domain.Account;
import org.example.fastrecklessbank.domain.AccountId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final ConcurrentHashMap<AccountId, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) {
        accounts.put(account.id(), account);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Collection<Account> findAll() {
        return accounts.values();
    }
}
