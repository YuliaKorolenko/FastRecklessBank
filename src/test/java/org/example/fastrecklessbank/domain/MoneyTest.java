package org.example.fastrecklessbank.domain;

import org.example.fastrecklessbank.common.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void allowsZeroAndPositive() {
        assertThat(Money.ofCents(0).cents()).isZero();
        assertThat(Money.ofCents(150).cents()).isEqualTo(150);
    }

    @Test
    void rejectsNegative() {
        assertThatThrownBy(() -> Money.ofCents(-10))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void ofPositiveCentsRejectsZeroAndNegative() {
        assertThatThrownBy(() -> Money.ofPositiveCents(0)).isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> Money.ofPositiveCents(-5)).isInstanceOf(InvalidAmountException.class);
        assertThat(Money.ofPositiveCents(5).cents()).isEqualTo(5);
    }

    @Test
    void plusAndMinus() {
        assertThat(Money.ofCents(100).plus(Money.ofCents(50))).isEqualTo(Money.ofCents(150));
        assertThat(Money.ofCents(100).minus(Money.ofCents(40))).isEqualTo(Money.ofCents(60));
    }

    @Test
    void isGreaterThanOrEqual() {
        assertThat(Money.ofCents(100).isGreaterThanOrEqual(Money.ofCents(100))).isTrue();
        assertThat(Money.ofCents(100).isGreaterThanOrEqual(Money.ofCents(101))).isFalse();
    }
}
