package demo.rest;

import java.util.UUID;

public record BigEntryTo(
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

    public static BigEntryTo breakpoint(final String comments) {
        return new BigEntryTo(
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

    public static BigEntryTo command(final String commands) {
        return new BigEntryTo(
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

    public static BigEntryTo displayFile(final String path, final String contentType, final Integer fromLine, final Integer numberOfLines, final Integer indent) {
        return new BigEntryTo(
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

    public static BigEntryTo heading(final HeadingLevel level, final String title) {
        return new BigEntryTo(
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

    public static BigEntryTo markdown(final String contents) {
        return new BigEntryTo(
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

    public static BigEntryTo todo(final String comments) {
        return new BigEntryTo(
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
}
