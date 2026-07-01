package org.example.fastrecklessbank.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank String name,
        @NotBlank String surname,
        Long initialDepositCents) {

    public long initialDepositOrZero() {
        return initialDepositCents == null ? 0L : initialDepositCents;
    }
}
