package org.example.fastrecklessbank.domain;

import java.util.UUID;

public record AccountId(UUID id) implements Comparable<AccountId> {

    @Override
    public int compareTo(AccountId other) {
        return this.id.compareTo(other.id);
    }
}
