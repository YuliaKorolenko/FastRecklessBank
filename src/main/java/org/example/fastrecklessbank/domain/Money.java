package org.example.fastrecklessbank.domain;

import org.example.fastrecklessbank.common.exception.BalanceLimitExceededException;
import org.example.fastrecklessbank.common.exception.InvalidAmountException;

import java.util.Locale;

/**
 * Money is stored in cent (long). A Money value is never negative and never
 * exceeds {@link #MAX_CENTS}.
 */
public record Money(long cents) {

    /** An amount may not exceed 10^16 dollars (10^18 cents). */
    public static final long MAX_CENTS = 1_000_000_000_000_000_000L;

    public Money {
        if (cents < 0) {
            throw new InvalidAmountException("Amount must not be negative: " + cents);
        }
        if (cents > MAX_CENTS) {
            throw new BalanceLimitExceededException(
                    "Amount cannot exceed " + Money.ofCents(MAX_CENTS).toDisplay());
        }
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public String toDisplay() {
        return String.format(Locale.US, "$%.2f", cents / 100.0);
    }

    /**
     * A strictly positive amount, used for deposit/withdraw/transfer operations.
     */
    public static Money ofPositiveCents(long cents) {
        if (cents <= 0) {
            throw new InvalidAmountException("Amount must be positive: " + cents);
        }
        return new Money(cents);
    }

    public Money plus(Money other) {
        return new Money(this.cents + other.cents);
    }

    public Money minus(Money other) {
        return new Money(this.cents - other.cents);
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.cents >= other.cents;
    }
}
