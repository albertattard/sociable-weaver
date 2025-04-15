package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonTypeName("Markdown")
public record Markdown(List<String> contents, Optional<List<String>> tags) implements Entry {

    public Markdown {
        contents = List.copyOf(contents);
        tags = tags.map(List::copyOf);
    }

    @Override
    public String runMarkdown() {
        return contents.stream()
                .map(line -> line.concat("\n"))
                .collect(Collectors.joining());
    }
}
