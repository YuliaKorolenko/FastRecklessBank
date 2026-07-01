package org.example.fastrecklessbank.common.exception;

/** Thrown when an account does not have enough balance for a withdrawal or transfer. */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
