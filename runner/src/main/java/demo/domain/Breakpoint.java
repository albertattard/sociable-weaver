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

    @Override
    public Result run() {
        final List<Stream<String>> streams = new ArrayList<>(3);
        streams.add(Stream.of("---", "", "# Breakpoint!"));

        comments.ifPresent(c -> {
            streams.add(Stream.of(""));
            streams.add(c.stream());
        });

        return Result.error(streams.stream()
                .flatMap(Function.identity())
                .map(line -> line + '\n')
                .collect(Collectors.joining()));
    }
}
