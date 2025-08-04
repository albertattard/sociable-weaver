package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import demo.json.OptionalListOfStringDeserializer;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

@JsonTypeName("Command")
public record Command(List<String> commands,
                      Optional<Duration> shouldFinishWithin,
                      Optional<Boolean> shouldFail,
                      Optional<List<String>> onFailureCommands,
                      Optional<List<String>> finallyCommands,
                      Optional<Path> workingDir,
                      Optional<CommandOutput> output,
                      Optional<List<String>> tags,
                      Optional<List<String>> comments,
                      OptionalInt indent) implements Entry {

    public record CommandOutput(Optional<Boolean> show,
                                @JsonDeserialize(using = OptionalListOfStringDeserializer.class)
                                Optional<List<String>> caption,
                                Optional<String> contentType) {

        public CommandOutput {
            requireNonNull(show);
            requireNonNull(caption);
            requireNonNull(contentType);

            caption = caption.map(List::copyOf);
        }
    }
}
