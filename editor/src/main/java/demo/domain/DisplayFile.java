package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@JsonTypeName("DisplayFile")
public record DisplayFile(Path path,
                          Optional<String> contentType,
                          OptionalInt fromLine,
                          OptionalInt numberOfLines,
                          Optional<List<String>> tags,
                          OptionalInt indent) implements Entry {}
