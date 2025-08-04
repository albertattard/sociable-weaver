package demo.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationDeserializer extends JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        final JsonNode node = parser.getCodec().readTree(parser);

        if (node.isMissingNode() || node.isNull()) {
            return null;
        }

        final String text = node.asText();
        if (text == null || text.isBlank()) {
            return null;
        }

        return parseDuration(text);
    }

    private static Duration parseDuration(final String text) {
        /* Regex to match units like "1 hour", "2 minutes", "3 seconds" */
        final Pattern pattern = Pattern.compile("(\\d+)\\s*(hour|minute|second)s?", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(text);

        Duration total = Duration.ZERO;

        while (matcher.find()) {
            final int value = Integer.parseInt(matcher.group(1));
            final String unit = matcher.group(2).toLowerCase();

            total = switch (unit) {
                case "hour" -> total.plusHours(value);
                case "minute" -> total.plusMinutes(value);
                case "second" -> total.plusSeconds(value);
                default -> total; /* TODO: Should we fail instead? */
            };
        }

        return total;
    }

    public static Module createModule() {
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(Duration.class, new DurationDeserializer());
        return module;
    }
}
