package org.example.fastrecklessbank.common.exception;

/** Thrown when no account exists for a given id. */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
