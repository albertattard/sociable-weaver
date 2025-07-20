package demo.domain;

public record EntryTo(
        String type,
        String comments,
        String commands,
        String path,
        String level,
        String title,
        String contents) {

    public static EntryTo heading(final String level, final String title) {
        return new EntryTo("Heading", null, null, null, level, title, null);
    }

    public EntryTo(final String type) {
        this(type, null, null, null, null, null, null);
    }
}
