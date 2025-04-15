package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import static java.util.Objects.requireNonNull;

@JsonTypeName("Heading")
public record Heading(HeadingLevel level, String title) implements Entry {

    public Heading {
        requireNonNull(level);
        requireNonNull(title);
    }

    public enum HeadingLevel {
        H1,
        H2,
        H3,
        H4,
        H5,
        ;

        public String toMarkdown() {
            return switch (this) {
                case H1 -> "#";
                case H2 -> "##";
                case H3 -> "###";
                case H4 -> "####";
                case H5 -> "#####";
            };
        }
    }

    @Override
    public String runMarkdown() {
        return level.toMarkdown() + " " + title + '\n';
    }
}
