package org.example.fastrecklessbank.infrastructure;

import org.example.fastrecklessbank.domain.AccountId;
import org.example.fastrecklessbank.domain.Money;
import org.example.fastrecklessbank.domain.TransferRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TransferStorageTest {

    private final AccountId owner = new AccountId(new UUID(0, 1));
    private final AccountId other = new AccountId(new UUID(0, 2));

    private TransferRecord record(long amount) {
        return new TransferRecord(owner, other, Money.ofCents(amount));
    }

    @Test
    void keepsInsertionOrderNewestFirst() {
        TransferStorage storage = new TransferStorage(50);
        TransferRecord first = record(100);
        TransferRecord second = record(200);
        TransferRecord third = record(300);
        storage.addRecord(first);
        storage.addRecord(second);
        storage.addRecord(third);

        assertThat(storage.getTransfersInProperOrder()).containsExactly(third, second, first);
    }

    @Test
    void retainsOnlyTheLast50AndEvictsOldestOnWraparound() {
        TransferStorage storage = new TransferStorage(50);
        for (int i = 1; i <= 60; i++) {
            storage.addRecord(record(i));
        }

        List<TransferRecord> recent = storage.getTransfersInProperOrder();
        assertThat(recent).hasSize(50);
        assertThat(recent.getFirst()).isEqualTo(record(60));
        assertThat(recent.getLast()).isEqualTo(record(11));
    }

    @Test
    void honoursCustomCapacity() {
        TransferStorage storage = new TransferStorage(3);
        storage.addRecord(record(1));
        storage.addRecord(record(2));
        storage.addRecord(record(3));
        storage.addRecord(record(4));
        assertThat(storage.getTransfersInProperOrder()).containsExactly(record(4), record(3), record(2));
    }

    /**
     * Many threads add concurrently to a buffer sized to hold every record (nothing is evicted).
     * With correct synchronization each add lands in its own slot, so all records survive and none
     * are clobbered. Without synchronization threads would race on the same slot/index and we would
     * see lost writes (fewer distinct amounts) or nulls.
     */
    @Test
    void concurrentAddsAreNotLost() throws InterruptedException {
        int threads = 16;
        int perThread = 1_000;
        int capacity = threads * perThread; // buffer exactly fits all writes -> no eviction
        TransferStorage storage = new TransferStorage(capacity);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            int base = t * perThread + 1; // each thread writes a unique, disjoint range of amounts
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        storage.addRecord(record(base + i));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        List<TransferRecord> all = storage.getTransfersInProperOrder();
        assertThat(all).hasSize(capacity);
        assertThat(all).doesNotContainNull();
        // every unique amount 1..capacity is present exactly once -> no write was lost or overwritten
        Set<Long> amounts = all.stream().map(r -> r.amount().cents()).collect(Collectors.toSet());
        assertThat(amounts).hasSize(capacity);
    }

    /**
     * A reader continuously snapshots the buffer while writers add. Synchronization guarantees the
     * reader only ever sees a fully-consistent state: never more than capacity elements and never a
     * null slot. Without it the reader could observe torn state (size incremented before the slot
     * is filled).
     */
    @Test
    void concurrentReadsNeverObserveTornState() throws InterruptedException {
        int capacity = 50;
        TransferStorage storage = new TransferStorage(capacity);
        int writers = 8;
        int perWriter = 5_000;

        ExecutorService pool = Executors.newFixedThreadPool(writers + 2);
        CountDownLatch writersDone = new CountDownLatch(writers);
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicReference<String> violation = new AtomicReference<>();

        Runnable reader = () -> {
            while (running.get()) {
                List<TransferRecord> snapshot = storage.getTransfersInProperOrder();
                if (snapshot.size() > capacity) {
                    violation.compareAndSet(null, "snapshot size " + snapshot.size() + " exceeds capacity");
                }
                if (snapshot.contains(null)) {
                    violation.compareAndSet(null, "snapshot contained a null element");
                }
            }
        };
        pool.submit(reader);
        pool.submit(reader);

        for (int w = 0; w < writers; w++) {
            int base = w * perWriter + 1;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perWriter; i++) {
                        storage.addRecord(record(base + i));
                    }
                } finally {
                    writersDone.countDown();
                }
            });
        }

        assertThat(writersDone.await(30, TimeUnit.SECONDS)).isTrue();
        running.set(false);
        pool.shutdownNow();

        assertThat(violation.get()).isNull();
    }
}
