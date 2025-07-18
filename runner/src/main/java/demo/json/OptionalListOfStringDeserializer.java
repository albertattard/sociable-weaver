package demo.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class OptionalListOfStringDeserializer extends JsonDeserializer<Optional<List<String>>> {

    @Override
    public Optional<List<String>> deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);

        if (node.isMissingNode() || node.isNull()) {
            return Optional.empty();
        }

        if (node.isTextual()) {
            return Optional.of(List.of(node.asText()));
        }

        if (node.isArray()) {
            final List<String> list = StreamSupport.stream(node.spliterator(), false)
                    .map(JsonNode::asText)
                    .toList();
            return Optional.of(list);
        }

        throw context.weirdStringException(node.toString(), List.class, "Expected string or array of strings");
    }
}
