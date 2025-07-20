package demo.rest;

import java.util.UUID;

public record EntryTo(
        String id,
        String type,
        String comments,
        String commands,
        String path,
        String level,
        String title,
        String contents) {

    public static EntryTo heading(final String level, final String title) {
        return new EntryTo(UUID.randomUUID().toString(), "Heading", null, null, null, level, title, null);
    }

    public EntryTo(final String type) {
        this(UUID.randomUUID().toString(), type, null, null, null, null, null, null);
    }
}
