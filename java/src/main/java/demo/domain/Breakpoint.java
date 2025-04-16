package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Optional;

@JsonTypeName("Breakpoint")
public record Breakpoint(Optional<List<String>> comments) implements Entry {

    public Breakpoint {
        comments = comments.map(List::copyOf);
    }

    @Override
    public String runMarkdown() {
        return "";
    }
}
