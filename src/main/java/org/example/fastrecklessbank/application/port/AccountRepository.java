package org.example.fastrecklessbank.application.port;

import org.example.fastrecklessbank.domain.Account;
import org.example.fastrecklessbank.domain.AccountId;

import java.util.Collection;
import java.util.Optional;

public interface AccountRepository {

    void createAccount(Account account);

    Optional<Account> findById(AccountId id);

    Collection<Account> findAll();
}
