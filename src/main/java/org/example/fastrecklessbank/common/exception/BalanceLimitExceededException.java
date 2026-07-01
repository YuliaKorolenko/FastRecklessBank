package org.example.fastrecklessbank.common.exception;

/** Thrown when an operation would push an account's balance above the allowed maximum. */
public class BalanceLimitExceededException extends RuntimeException {
    public BalanceLimitExceededException(String message) {
        super(message);
    }
}
