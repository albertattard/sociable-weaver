package demo.rest;

import java.util.UUID;

public record EntryTo(
        UUID id,
        EntryType type,
        String comments,
        String commands,
        String path,
        HeadingLevel level,
        String title,
        String contents) {

    public static EntryTo heading(final HeadingLevel level, final String title) {
        return new EntryTo(UUID.randomUUID(), EntryType.Heading, null, null, null, level, title, null);
    }

    public EntryTo(final EntryType type) {
        this(UUID.randomUUID(), type, null, null, null, null, null, null);
    }
}
