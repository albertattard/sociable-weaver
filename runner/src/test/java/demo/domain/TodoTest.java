package demo.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TodoTest {

    @Nested
    class DeserializeTests {

        @Test
        void returnDeserializedTodoWhenGivenMinimumOptions() {
            final String json = """
                    {
                       "entries": [
                         {
                           "type": "Todo"
                         }
                       ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Todo(Optional.empty()))));
        }

        @Test
        void returnDeserializedTodoWhenGivenAllOptions() {
            final String json = """
                    {
                       "entries": [
                         {
                           "type": "Todo",
                           "comments": ["Testing todos"]
                         }
                       ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Todo(Optional.of(List.of("Testing todos"))))));
        }
    }

    @Nested
    class RunTests {

        @Test
        void returnTheResultWithoutAdditionalComments() {
            final Entry entry = new Todo(Optional.empty());

            final Result result = entry.run();

            final String expected = """
                    [//]: # TODO
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void returnTheResultWithTheAdditionalComments() {
            final Entry entry = new Todo(Optional.of(List.of("Comment 1", "Comment 2")));

            final Result result = entry.run();

            final String expected = """
                    [//]: # TODO
                    [//]: # Comment 1
                    [//]: # Comment 2
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }
    }
}