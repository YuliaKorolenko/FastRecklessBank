package org.example.fastrecklessbank.domain;

import org.example.fastrecklessbank.common.exception.InsufficientFundsException;

import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private final AccountId id;
    private final String name;
    private final String surname;
    private Money balance;
    private final ReentrantLock lock = new ReentrantLock();

    public Account(AccountId id, String name, String surname, Money balance) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.balance = balance;
    }

    public void deposit(Money amount) {
        balance = balance.plus(amount);
    }

    public void withdraw(Money amount) {
        if (!balance.isGreaterThanOrEqual(amount)) {
            throw new InsufficientFundsException(
                    "Account " + id.id() + " has insufficient funds: balance="
                            + balance.cents() + ", requested=" + amount.cents());
        }
        balance = balance.minus(amount);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public AccountId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String surname() {
        return surname;
    }

    public Money balance() {
        return balance;
    }
}
