package demo.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record Document(List<Entry> entries) {

    public Document {
        entries = List.copyOf(entries);
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

    private static ObjectMapper createMapper() {
        return JsonMapper.builder()
                .addModule(new Jdk8Module())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .registerSubtypes(
                        Breakpoint.class,
                        DisplayFile.class,
                        Heading.class,
                        Markdown.class)
                .build();
    }
}
