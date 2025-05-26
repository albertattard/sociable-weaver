package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonTypeName("Todo")
public record Todo(Optional<List<String>> comments) implements Entry {

    public Todo {
        comments = comments.map(List::copyOf);
    }

    @Override
    public Result run() {
        return Result.ok(Stream.of(Stream.of("TODO"), comments.stream().flatMap(List::stream))
                .flatMap(Function.identity())
                .map(line -> "[//]: # " + line + '\n')
                .collect(Collectors.joining()));
    }
}
