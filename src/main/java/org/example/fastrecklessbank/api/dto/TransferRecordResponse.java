package org.example.fastrecklessbank.api.dto;

import org.example.fastrecklessbank.domain.TransferRecord;

import java.util.UUID;

/** API view of a single outgoing transfer. */
public record TransferRecordResponse(
        UUID fromAccountId,
        UUID toAccountId,
        long amountCents) {

    public static TransferRecordResponse from(TransferRecord record) {
        return new TransferRecordResponse(
                record.fromAccount().id(),
                record.toAccount().id(),
                record.amount().cents());
    }
}
