package demo.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import demo.json.DurationDeserializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public record Document(List<Entry> entries) {

    public Document {
        entries = List.copyOf(entries);
    }

    public static Document parse(final Path file) {
        requireNonNull(file);

        return parse(readSwPlaybookFromFile(file));
    }

    public static Document parse(final String json) {
        requireNonNull(json);

        try {
            return createMapper().
                    readValue(json, Document.class);
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse the JSON object", e);
        }
    }

    public void run(final Consumer<String> output) {
        /* This is used to run the finally blocks in rever order starting from the last processed index */
        int lastProcessedIndex = -1;

        runLoop:
        for (int runIndex = 0; runIndex < entries.size(); runIndex++) {
            lastProcessedIndex = runIndex;
            final Entry entry = entries.get(lastProcessedIndex);
            final Result result = entry.run();

            switch (result) {
                case Result.Ok ok -> output.accept(ok.output());
                case Result.Error e -> {
                    output.accept(e.error());
                    break runLoop;
                }
            }
        }

        for (int runFinallyIndex = lastProcessedIndex; runFinallyIndex >= 0; runFinallyIndex--) {
            entries.get(runFinallyIndex).runFinally();
        }
    }

    private static ObjectMapper createMapper() {
        return JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(DurationDeserializer.createModule())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .registerSubtypes(
                        Breakpoint.class,
                        Command.class,
                        DisplayFile.class,
                        Heading.class,
                        Markdown.class,
                        Todo.class)
                .build();
    }

    private static String readSwPlaybookFromFile(final Path path) {
        try {
            return Files.readString(path);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read the Sociable Weaver playbook file: " + path, e);
        }
    }
}
