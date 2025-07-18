package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@JsonTypeName("DisplayFile")
public record DisplayFile(Path path,
                          Optional<String> contentType,
                          OptionalInt fromLine,
                          OptionalInt numberOfLines,
                          Optional<List<String>> tags,
                          OptionalInt indent) implements Entry {
    @Override
    public Result run() {
        final String contentType = computeContentType();

        try (Stream<Stream<String>> stream = Stream.of(Stream.of("```" + contentType), readLines(), Stream.of("```"))) {
            return Result.ok(stream.flatMap(Function.identity())
                    .map(indentLines())
                    .map(line -> line + '\n')
                    .collect(Collectors.joining()));
        }
    }

    private Function<String, String> indentLines() {
        return indent.isEmpty()
                ? Function.identity()
                : line -> line.isBlank() ? line : (" ".repeat(indent.getAsInt())).concat(line);
    }

    private Stream<String> readLines() {
        try {
            Stream<String> lines = Files.lines(expandTilde(), UTF_8);

            if (fromLine.isPresent()) {
                lines = lines.skip(fromLine.getAsInt() - 1);
            }

            if (numberOfLines.isPresent()) {
                lines = lines.limit(numberOfLines.getAsInt());
            }

            return lines;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file " + path, e);
        }
    }

    private String computeContentType() {
        /* TODO: Handle Dockerfiles well, as these do not have an extension */
        return contentType.orElseGet(() -> pathExtension().orElse(""));
    }

    private Optional<String> pathExtension() {
        final String fileName = path.getFileName().toString();
        final int index = fileName.lastIndexOf('.');
        return index < 0 || index >= fileName.length() - 1
                ? Optional.empty()
                : Optional.of(fileName.substring(index + 1));
    }

    public Path expandTilde() {
        return path.startsWith("~")
                ? (path.getNameCount() == 1
                ? Path.of(System.getProperty("user.home"))
                : Path.of(System.getProperty("user.home")).resolve(path.subpath(1, path.getNameCount())))
                : path;
    }
}
