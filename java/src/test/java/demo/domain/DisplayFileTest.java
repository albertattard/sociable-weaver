package demo.domain;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class DisplayFileTest {

    @Nested
    class DeserializeTests {

        @Test
        void returnDeserializedMarkdownWhenGivenMinimumOptions() {
            final String json = """
                    {
                       "entries": [
                         {
                           "type": "DisplayFile",
                           "path": "./some/path/File.java"
                         }
                       ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new DisplayFile(Path.of("./some/path/File.java"), Optional.empty(), OptionalInt.empty(), OptionalInt.empty(), Optional.empty(), OptionalInt.empty()))));
        }

        @Test
        void returnDeserializedMarkdownWhenGivenAllOptions() {
            final String json = """
                    {
                       "entries": [
                         {
                           "type": "DisplayFile",
                           "content_type": "something",
                           "from_line": 5,
                           "number_of_lines": 3,
                           "path": "./some/path/File.java",
                           "indent": 2,
                           "tags": ["test"]
                         }
                       ]
                    }""";

            final Document parsed = Document.parse(json);

            assertThat(parsed)
                    .isEqualTo(new Document(List.of(new DisplayFile(Path.of("./some/path/File.java"), Optional.of("something"), OptionalInt.of(5), OptionalInt.of(3), Optional.of(List.of("test")), OptionalInt.of(2)))));
        }
    }

    @Nested
    class RunTests {
        @Test
        void displayJavaFile() {
            final String javaFile = """
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main1 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    """;

            final Path file = writeFixture(javaFile, Path.of("target", "fixtures", "Main1.java"));

            final Entry entry = new DisplayFile(file, Optional.empty(), OptionalInt.empty(), OptionalInt.empty(), Optional.empty(), OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```java
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main1 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void displayJavaFileThatDoesNotHaveNewLineAtTheEnd() {
            final String javaFile = """
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main2 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }""";

            final Path file = writeFixture(javaFile, Path.of("target", "fixtures", "Main2.java"));

            final Entry entry = new DisplayFile(file, Optional.empty(), OptionalInt.empty(), OptionalInt.empty(), Optional.empty(), OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```java
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main2 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void displaySomeLinesFromFile() {
            final String javaFile = """
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main3 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    """;

            final Path file = writeFixture(javaFile, Path.of("target", "fixtures", "Main3.java"));

            final Entry entry = new DisplayFile(file, Optional.empty(), OptionalInt.of(9), OptionalInt.of(3), Optional.empty(), OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```java
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void displayFileSpecificContentType() {
            final String javaFile = """
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main4 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    """;

            final Path file = writeFixture(javaFile, Path.of("target", "fixtures", "Main4.java"));

            final Entry entry = new DisplayFile(file, Optional.of("txt"), OptionalInt.empty(), OptionalInt.empty(), Optional.empty(), OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```txt
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main4 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void displaySomeLinesFromFileAndApplyAdditionalIndentation() {
            final String javaFile = """
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main5 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }""";

            final Path file = writeFixture(javaFile, Path.of("target", "fixtures", "Main5.java"));

            final Entry entry = new DisplayFile(file, Optional.empty(), OptionalInt.of(9), OptionalInt.of(3), Optional.empty(), OptionalInt.of(3));

            final Result result = entry.run();

            final String expected = """
                       ```java
                           public static void main(final String[] args) {
                               SpringApplication.run(Main.class, args);
                           }
                       ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @Test
        void handelPathStartingWithTilda() {
            final String javaFile = """
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main6 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    """;

            writeFixture(javaFile, Path.of(System.getProperty("user.home"), ".tmp", "Main6.java"));

            final Entry entry = new DisplayFile(Path.of("~/.tmp/Main6.java"), Optional.empty(), OptionalInt.empty(), OptionalInt.empty(), Optional.empty(), OptionalInt.empty());

            final Result result = entry.run();

            final String expected = """
                    ```java
                    package demo;
                    
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    
                    @SpringBootApplication
                    public class Main6 {
                    
                        public static void main(final String[] args) {
                            SpringApplication.run(Main.class, args);
                        }
                    }
                    ```
                    """;

            assertThat(result)
                    .isEqualTo(Result.ok(expected));
        }

        @AfterAll
        static void deleteTestFixtures() {
            deleteRecursively(Path.of(System.getProperty("user.home"), ".tmp"));
        }

        private static Path writeFixture(final String contents, final Path file) {
            try {
                Files.createDirectories(file.toAbsolutePath().getParent());
                Files.writeString(file, contents, UTF_8);
                return file;
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to write fixture", e);
            }
        }

        private static void deleteRecursively(final Path path) {
            if (!Files.exists(path)) return;

            if (Files.isDirectory(path)) {
                try (Stream<Path> entries = Files.list(path)) {
                    for (Path entry : entries.toList()) {
                        deleteRecursively(entry);
                    }
                } catch (final IOException e) {
                    throw new UncheckedIOException("Failed to list files under the directory: " + path, e);
                }
            }

            try {
                Files.delete(path);
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to delete path: " + path, e);
            }
        }
    }
}