package demo.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static demo.domain.Heading.HeadingLevel.H1;
import static demo.domain.Heading.HeadingLevel.H2;
import static demo.domain.Heading.HeadingLevel.H3;
import static demo.domain.Heading.HeadingLevel.H4;
import static demo.domain.Heading.HeadingLevel.H5;
import static org.assertj.core.api.Assertions.assertThat;

class HeadingTest {

    @Nested
    class DeserializeTests {

        @Test
        void returnDeserializedHeading() {
            final String json = """
                    {
                      "entries": [
                        {
                          "type": "Heading",
                          "level": "H1",
                          "title": "Prologue"
                        }
                      ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Heading(H1, "Prologue"))));
        }
    }

    @Nested
    class MarkdownRunnableTests {

        @Test
        void formatH1() {
            final Entry entry = new Heading(H1, "Heading Level 1");

            final String markdown = entry.runMarkdown();

            assertThat(markdown)
                    .isEqualTo("""
                            # Heading Level 1
                            """);
        }

        @Test
        void formatH2() {
            final Entry entry = new Heading(H2, "Heading Level 2");

            final String markdown = entry.runMarkdown();

            assertThat(markdown)
                    .isEqualTo("""
                            ## Heading Level 2
                            """);
        }

        @Test
        void formatH3() {
            final Entry entry = new Heading(H3, "Heading Level 3");

            final String markdown = entry.runMarkdown();

            assertThat(markdown)
                    .isEqualTo("""
                            ### Heading Level 3
                            """);
        }

        @Test
        void formatH4() {
            final Entry entry = new Heading(H4, "Heading Level 4");

            final String markdown = entry.runMarkdown();

            assertThat(markdown)
                    .isEqualTo("""
                            #### Heading Level 4
                            """);
        }

        @Test
        void formatH5() {
            final Entry entry = new Heading(H5, "Heading Level 5");

            final String markdown = entry.runMarkdown();

            assertThat(markdown)
                    .isEqualTo("""
                            ##### Heading Level 5
                            """);
        }
    }
}