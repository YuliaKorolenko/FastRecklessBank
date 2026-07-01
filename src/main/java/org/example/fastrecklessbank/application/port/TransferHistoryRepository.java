package org.example.fastrecklessbank.application.port;

import org.example.fastrecklessbank.domain.AccountId;
import org.example.fastrecklessbank.domain.TransferRecord;

import java.util.List;

public interface TransferHistoryRepository {

    /** Records a transfer for the given  account. */
    void record(AccountId owner, TransferRecord transfer);

    /** Returns the owner's recent transfers, newest first. */
    List<TransferRecord> getRecentTransfers(AccountId owner);
}
