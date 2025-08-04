package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonTypeName("Breakpoint")
public record Breakpoint(Optional<List<String>> comments) implements Entry {

    public Breakpoint {
        comments = comments.map(List::copyOf);
    }
}
