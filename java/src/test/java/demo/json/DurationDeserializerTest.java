package demo.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DurationDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(DurationDeserializer.createModule())
                .build();
    }

    @Test
    void deserializeOnlyHours() throws JsonProcessingException {
        final TestData testData = deserialize("{\"timeout\": \"2 hours\"}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.of(Duration.ofHours(2)));
    }

    @Test
    void deserializeOnlyMinutes() throws JsonProcessingException {
        final TestData testData = deserialize("{\"timeout\": \"15 minutes\"}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.of(Duration.ofMinutes(15)));
    }

    @Test
    void deserializeOnlySeconds() throws JsonProcessingException {
        final TestData testData = deserialize("{\"timeout\": \"45 seconds\"}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.of(Duration.ofSeconds(45)));
    }

    @Test
    void deserializeHoursMinutesAndSeconds() throws JsonProcessingException {
        final TestData testData = deserialize("{\"timeout\": \"1 hour 30 minutes 15 seconds\"}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.of(Duration.ofHours(1).plusMinutes(30).plusSeconds(15)));
    }

    @Test
    void deserializeCaseInsensitive() throws JsonProcessingException {
        final TestData testData = deserialize("{\"timeout\": \"2 HOURS 5 Minutes\"}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.of(Duration.ofHours(2).plusMinutes(5)));
    }

    /* TODO: This test is failing for an unknown reason. Further investigation is needed. */
    @Test
    void deserializeMissingFieldAsOptionalEmpty() throws JsonProcessingException {
        final TestData testData = deserialize("{}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.empty());
    }

    @Test
    void deserializeEmptyStringAsOptionalEmpty() throws JsonProcessingException {
        final TestData testData = deserialize("{\"timeout\": \"\"}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.empty());
    }

    @Test
    void ignoreInvalidUnits() throws JsonProcessingException {
        /* TODO: Should we fail instead? */
        /* Only "10 minutes" should be parsed */
        final TestData testData = deserialize("{\"timeout\": \"5 monkeys 10 minutes\"}");
        assertThat(testData.timeout())
                .isEqualTo(Optional.of(Duration.ofMinutes(10)));
    }

    private TestData deserialize(final String json) throws JsonProcessingException {
        return objectMapper.readValue(json, TestData.class);
    }

    public record TestData(Optional<Duration> timeout) {}
}
