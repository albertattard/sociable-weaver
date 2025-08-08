package demo.web;

import demo.domain.*;
import demo.domain.Heading.HeadingLevel;

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

    public static BigEntryTo of(final Entry entry) {
        return switch (entry) {
            case Breakpoint breakpoint -> breakpoint(breakpoint);
            case Command command -> command(command);
            case DisplayFile displayFile -> displayFile(displayFile);
            case Heading heading -> heading(heading);
            case Markdown markdown -> markdown(markdown);
            case Todo todo -> todo(todo);
        };
    }

    private static BigEntryTo breakpoint(final Breakpoint breakpoint) {
        return new BigEntryTo(
                UUID.randomUUID(),
                EntryType.Breakpoint,
                breakpoint.comments().map(l -> String.join("\n", l)).orElse(""),
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

    private static BigEntryTo command(final Command command) {
        return new BigEntryTo(
                UUID.randomUUID(),
                EntryType.Command,
                null,
                String.join("\n", command.commands()),
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

    private static BigEntryTo displayFile(final DisplayFile displayFile) {
        return new BigEntryTo(
                UUID.randomUUID(),
                EntryType.DisplayFile,
                null,
                null,
                displayFile.path().toString(),
                displayFile.contentType().orElse(""),
                displayFile.fromLine().stream().boxed().findFirst().orElse(null),
                displayFile.numberOfLines().stream().boxed().findFirst().orElse(null),
                displayFile.indent().stream().boxed().findFirst().orElse(null),
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

    private static BigEntryTo heading(final Heading heading) {
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
                heading.level(),
                heading.title(),
                null);
    }

    public static BigEntryTo heading(final Heading.HeadingLevel level, final String title) {
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

    private static BigEntryTo markdown(final Markdown markdown) {
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
                String.join("\n", markdown.contents()));
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

    private static BigEntryTo todo(final Todo todo) {
        return new BigEntryTo(
                UUID.randomUUID(),
                EntryType.Todo,
                todo.comments().map(l -> String.join("\n", l)).orElse(""),
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
