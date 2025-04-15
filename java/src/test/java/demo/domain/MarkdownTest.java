package demo.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownTest {

    @Nested
    class DeserializeTests {
        @Test
        void returnDeserializedMarkdownWhenGivenMinimumOptions() {
            final String json = """
                    {
                      "entries": [
                        {
                          "type": "Markdown",
                          "contents": [
                            "We make mistakes, and we make more mistakes, and some more, and that’s how we learn."
                          ]
                        }
                      ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Markdown(List.of("We make mistakes, and we make more mistakes, and some more, and that’s how we learn."), Optional.empty()))));
        }

        @Test
        void returnDeserializedMarkdownWhenGivenAllOptions() {
            final String json = """
                    {
                      "entries": [
                        {
                          "type": "Markdown",
                          "contents": [
                            "We make mistakes, and we make more mistakes, and some more, and that’s how we learn."
                          ],
                          "tags": [
                            "test"
                          ]
                        }
                      ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Markdown(List.of("We make mistakes, and we make more mistakes, and some more, and that’s how we learn."), Optional.of(List.of("test"))))));
        }
    }

    @Nested
    class MarkdownRunnableTests {
        @Test
        void format() {
            final Entry entry = new Markdown(List.of("We make mistakes, and we make more mistakes, and some more, and that’s how we", "learn."), Optional.of(List.of("test")));

            final String markdown = entry.runMarkdown();

            assertThat(markdown)
                    .isEqualTo("""
                            We make mistakes, and we make more mistakes, and some more, and that’s how we
                            learn.
                            """);
        }
    }
}
