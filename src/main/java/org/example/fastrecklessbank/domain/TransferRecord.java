package org.example.fastrecklessbank.domain;

public record TransferRecord(AccountId fromAccount, AccountId toAccount, Money amount) {

}
