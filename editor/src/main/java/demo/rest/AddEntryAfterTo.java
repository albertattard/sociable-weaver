package demo.rest;

import java.util.UUID;

public record AddEntryAfterTo(
        UUID id,
        EntryType type) {

    public EntryTo toNewEntry() {
        return switch (type) {
            case Heading ->
                    new EntryTo(
                            UUID.randomUUID(),
                            type,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            HeadingLevel.H2,
                            "Heading...",
                            null);
            default -> new EntryTo(
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
        };
    }
}
