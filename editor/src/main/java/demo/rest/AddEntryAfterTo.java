package demo.rest;

import java.util.UUID;

public record AddEntryAfterTo(
        UUID id,
        EntryType type) {

    public BigEntryTo toNewEntry() {
        return new BigEntryTo(
                UUID.randomUUID(),
                type,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
