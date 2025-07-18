package demo.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static demo.domain.Heading.HeadingLevel.*;
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
    class RunTests {

        @Test
        void formatH1() {
            final Entry entry = new Heading(H1, "Heading Level 1");

            final Result result = entry.run();

            assertThat(result)
                    .isEqualTo(Result.ok("""
                            # Heading Level 1
                            """));
        }

        @Test
        void formatH2() {
            final Entry entry = new Heading(H2, "Heading Level 2");

            final Result result = entry.run();

            assertThat(result)
                    .isEqualTo(Result.ok("""
                            ## Heading Level 2
                            """));
        }

        @Test
        void formatH3() {
            final Entry entry = new Heading(H3, "Heading Level 3");

            final Result result = entry.run();

            assertThat(result)
                    .isEqualTo(Result.ok("""
                            ### Heading Level 3
                            """));
        }

        @Test
        void formatH4() {
            final Entry entry = new Heading(H4, "Heading Level 4");

            final Result result = entry.run();

            assertThat(result)
                    .isEqualTo(Result.ok("""
                            #### Heading Level 4
                            """));
        }

        @Test
        void formatH5() {
            final Entry entry = new Heading(H5, "Heading Level 5");

            final Result result = entry.run();

            assertThat(result)
                    .isEqualTo(Result.ok("""
                            ##### Heading Level 5
                            """));
        }
    }
}