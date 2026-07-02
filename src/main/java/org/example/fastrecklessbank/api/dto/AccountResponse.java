package org.example.fastrecklessbank.api.dto;

import org.example.fastrecklessbank.domain.Account;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        String surname,
        String balanceCents) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.id().id(),
                account.name(),
                account.surname(),
                Long.toString(account.balance().cents()));
    }
}
