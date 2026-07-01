package org.example.fastrecklessbank.domain;

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
}
