package org.example.fastrecklessbank.common.exception;

/** Thrown when a monetary amount is invalid (negative, or zero where a positive amount is required). */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
