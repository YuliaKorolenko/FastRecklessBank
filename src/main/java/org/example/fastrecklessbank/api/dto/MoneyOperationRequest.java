package org.example.fastrecklessbank.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request body for deposit and withdraw operations. */
public record MoneyOperationRequest(
        @NotNull @Positive Long amountCents) {
}
