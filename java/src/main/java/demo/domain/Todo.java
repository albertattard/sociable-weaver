package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Optional;

@JsonTypeName("Todo")
public record Todo(Optional<List<String>> comments) implements Entry {

    public Todo {
        comments = comments.map(List::copyOf);
    }

    @Override
    public String runMarkdown() {
        return "";
    }
}
