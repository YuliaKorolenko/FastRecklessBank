package org.example.fastrecklessbank.infrastructure;

import org.example.fastrecklessbank.application.port.TransferHistoryRepository;
import org.example.fastrecklessbank.domain.AccountId;
import org.example.fastrecklessbank.domain.TransferRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTransferHistoryRepository implements TransferHistoryRepository {

    private static final int MAX_HISTORY_SIZE = 50;

    private final ConcurrentHashMap<AccountId, TransferStorage> transferHistory = new ConcurrentHashMap<>();

    @Override
    public void record(AccountId owner, TransferRecord transfer) {
        transferHistory
                .computeIfAbsent(owner, id -> new TransferStorage(MAX_HISTORY_SIZE))
                .addRecord(transfer);
    }

    @Override
    public List<TransferRecord> getRecentTransfers(AccountId owner) {
        TransferStorage storage = transferHistory.get(owner);
        return storage == null ? List.of() : storage.getTransfersInProperOrder();
    }
}
