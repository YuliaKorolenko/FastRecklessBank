package org.example.fastrecklessbank.infrastructure;

import org.example.fastrecklessbank.domain.TransferRecord;

import java.util.ArrayList;
import java.util.List;

public class TransferStorage {
    private final TransferRecord[] records;
    private final int maxHistorySize;
    private int oldestIndex;
    private int size;

    public TransferStorage(int maxHistorySize) {
        if (maxHistorySize <= 0) {
            throw new IllegalArgumentException("maxHistorySize must be positive: " + maxHistorySize);
        }
        this.maxHistorySize = maxHistorySize;
        this.records = new TransferRecord[maxHistorySize];
        this.oldestIndex = 0;
        this.size = 0;
    }

    public synchronized void addRecord(TransferRecord transferRecord) {
        int writeIndex = (oldestIndex + size) % maxHistorySize;
        records[writeIndex] = transferRecord;
        if (size < maxHistorySize) {
            size++;
        } else {
            oldestIndex = (oldestIndex + 1) % maxHistorySize;
        }
    }

    public synchronized List<TransferRecord> getTransfersInProperOrder() {
        List<TransferRecord> result = new ArrayList<>(size);
        for (int i = size - 1; i >= 0; i--) {
            result.add(records[(oldestIndex + i) % maxHistorySize]);
        }
        return result;
    }

    public synchronized int size() {
        return size;
    }
}
