package demo.rest;

import java.util.UUID;

public record EntryTo(
        UUID id,
        EntryType type,
        String comments,
        String commands,
        String path,
        String contentType,
        Integer fromLine,
        Integer numberOfLines,
        Integer indent,
        HeadingLevel level,
        String title,
        String contents) {

    public static EntryTo breakpoint(final String comments) {
        return new EntryTo(
                UUID.randomUUID(),
                EntryType.Breakpoint,
                comments,
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

    public static EntryTo command(final String commands) {
        return new EntryTo(
                UUID.randomUUID(),
                EntryType.Command,
                null,
                commands,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public static EntryTo displayFile(final String path, final String contentType, final Integer fromLine, final Integer numberOfLines, final Integer indent) {
        return new EntryTo(
                UUID.randomUUID(),
                EntryType.DisplayFile,
                null,
                null,
                path,
                contentType,
                fromLine,
                numberOfLines,
                indent,
                null,
                null,
                null);
    }

    public static EntryTo heading(final HeadingLevel level, final String title) {
        return new EntryTo(
                UUID.randomUUID(),
                EntryType.Heading,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                level,
                title,
                null);
    }

    public static EntryTo markdown(final String contents) {
        return new EntryTo(
                UUID.randomUUID(),
                EntryType.Markdown,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                contents);
    }

    public static EntryTo todo(final String comments) {
        return new EntryTo(
                UUID.randomUUID(),
                EntryType.Todo,
                comments,
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

    public EntryTo(final EntryType type) {
        this(
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
