package demo.rest;

import java.util.UUID;

public record AddEntryAfterTo(
        UUID id,
        EntryType type) {

    public EntryTo toNewEntry() {
        return new EntryTo(
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
