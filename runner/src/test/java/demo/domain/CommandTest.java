package demo.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CommandTest {

    @Nested
    class DeserializeTests {

        @Test
        void returnDeserializedCommandWhenGivenMinimumOptions() {
            final String json = """
                    {
                       "entries": [
                         {
                           "type": "Command",
                            "commands": [
                              "echo 'Hello there!'"
                            ]
                         }
                       ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Command(
                            List.of("echo 'Hello there!'"),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            OptionalInt.empty()))));
        }

        @Test
        void returnDeserializedCommandWhenGivenAllOptions() {
            final String json = """
                    {
                      "entries": [
                        {
                          "type": "Command",
                          "commands": [
                            "echo 'Hello there!'"
                          ],
                          "should_finish_within": "3 seconds",
                          "should_fail": true,
                          "on_failure_commands": [
                            "echo 'Failed to say hello there!'"
                          ],
                          "finally_commands": [
                            "echo 'Running cleanup!'"
                          ],
                          "working_dir": "dir",
                          "output": {
                            "show": false,
                            "caption": "The output is hidden",
                            "content_type": "xml"
                          },
                          "comments": [
                            "test",
                            "comments"
                          ],
                          "tags": [
                            "test",
                            "tags"
                          ],
                          "indent": 3
                        }
                      ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Command(
                            List.of("echo 'Hello there!'"),
                            Optional.of(Duration.ofSeconds(3)),
                            Optional.of(true),
                            Optional.of(List.of("echo 'Failed to say hello there!'")),
                            Optional.of(List.of("echo 'Running cleanup!'")),
                            Optional.of(Path.of("dir")),
                            Optional.of(new Command.CommandOutput(Optional.of(false), Optional.of(List.of("The output is hidden")), Optional.of("xml"))),
                            Optional.of(List.of("test", "tags")),
                            Optional.of(List.of("test", "comments")),
                            OptionalInt.of(3)))));
        }

        @Test
        void returnDeserializedCommandWhenGivenOutputCaptionAsList() {
            final String json = """
                    {
                      "entries": [
                        {
                          "type": "Command",
                          "commands": [
                            "echo 'Hello there!'"
                          ],
                          "output": {
                            "caption": [
                              "The output is visible"
                            ]
                          }
                        }
                      ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new Command(
                            List.of("echo 'Hello there!'"),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.of(new Command.CommandOutput(Optional.empty(), Optional.of(List.of("The output is visible")), Optional.empty())),
                            Optional.empty(),
                            Optional.empty(),
                            OptionalInt.empty()))));
        }
    }

    @Nested
    class RunTests {
        @Test
        void runSingleCommandWithoutShowingOutput() {
            final Entry entry = new Command(
                    List.of("echo 'Hello there!'"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```shell
                    echo 'Hello there!'
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void runSingleCommandAndShowingOutput() {
            final Entry entry = new Command(
                    List.of("echo 'Hello there!'"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new Command.CommandOutput(Optional.empty(), Optional.empty(), Optional.empty())),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```shell
                    echo 'Hello there!'
                    ```
                    
                    _Output_
                    
                    ```
                    Hello there!
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void runSingleCommandAndShowOutputWithCustomContentType() {
            final Entry entry = new Command(
                    List.of("echo '{\"name\": \"Albert Attard\"}'"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new Command.CommandOutput(Optional.of(true), Optional.empty(), Optional.of("json"))),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```shell
                    echo '{"name": "Albert Attard"}'
                    ```
                    
                    _Output_
                    
                    ```json
                    {"name": "Albert Attard"}
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void runMultipleCommandsAndShowingOutput() {
            final Entry entry = new Command(
                    List.of("echo 1", "echo 2"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new Command.CommandOutput(Optional.empty(), Optional.empty(), Optional.empty())),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```shell
                    echo 1
                    echo 2
                    ```
                    
                    _Output_
                    
                    ```
                    1
                    2
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void runIndentedCommandAndShowingOutput() {
            final Entry entry = new Command(
                    List.of("echo 'Hello there!'"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new Command.CommandOutput(Optional.empty(), Optional.empty(), Optional.empty())),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.of(3));

            final Result result = entry.run();

            final String expected = """
                       ```shell
                       echo 'Hello there!'
                       ```
                    
                       _Output_
                    
                       ```
                       Hello there!
                       ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void runCommandWithinWorkingDirectoryAndShowingOutput() {
            final Entry entry = new Command(
                    List.of("pwd | awk -F/ '{print $(NF-2) \"/\" $(NF-1) \"/\" $NF}'"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(Path.of("target")),
                    Optional.of(new Command.CommandOutput(Optional.empty(), Optional.empty(), Optional.empty())),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```shell
                    # Running command from within the 'target' directory
                    (cd 'target'
                     pwd | awk -F/ '{print $(NF-2) "/" $(NF-1) "/" $NF}'
                    )
                    ```
                    
                    _Output_
                    
                    ```
                    sociable-weaver/runner/target
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void runOnErrorWhenCommandFails() {
            final Entry entry = new Command(
                    List.of("failing on purpose"),
                    Optional.empty(),
                    Optional.of(true),
                    Optional.of(List.of("cat << EOF > './target/error.txt'",
                            "It failed!",
                            "EOF")),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.empty());

            final Result result = entry.run();
            final String output = readString(Path.of("target", "error.txt"));

            /* TODO: Check that the output is OK */
//            assertThat(result)
//                    .isInstanceOfAny(Result.Ok.class);

            assertThat(output)
                    .isEqualTo("""
                            It failed!
                            """);
        }

        @Test
        void runCommandWithLongOutput() {
            final Entry entry = new Command(
                    List.of("i=1; while [ \"${i}\" -le 10000 ]; do echo \"[${i}] The quick brown fox jumps over the lazy dog!\"; i=$((i + 1)); done"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new Command.CommandOutput(Optional.empty(), Optional.empty(), Optional.empty())),
                    Optional.empty(),
                    Optional.empty(),
                    OptionalInt.empty());

            final Result result = entry.run();

            final String header = """
                    ```shell
                    i=1; while [ "${i}" -le 10000 ]; do echo "[${i}] The quick brown fox jumps over the lazy dog!"; i=$((i + 1)); done
                    ```
                    
                    _Output_
                    
                    ```
                    """;

            final String output = IntStream.rangeClosed(1, 10000)
                    .mapToObj(i -> "[" + i + "] The quick brown fox jumps over the lazy dog!")
                    .map(line -> line + '\n')
                    .collect(Collectors.joining());

            final String footer = """
                    ```
                    """;

            final String expected = header + output + footer;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        private static String readString(final Path path) {
            try {
                return Files.readString(path);
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to read file " + path, e);
            }
        }
    }
}
