package demo.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BreakpointTest {

    @Nested
    class DeserializeTests {

        @Test
        void returnDeserializedMarkdownWhenGivenMinimumOptions() {
            final String json = """
                    {
                       "entries": [
                         {
                           "type": "Breakpoint"
                         }
                       ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Breakpoint(Optional.empty()))));
        }

        @Test
        void returnDeserializedBreakpointWhenGivenAllOptions() {
            final String json = """
                    {
                       "entries": [
                         {
                           "type": "Breakpoint",
                           "comments": ["Testing breakpoints"]
                         }
                       ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Breakpoint(Optional.of(List.of("Testing breakpoints"))))));
        }
    }
}