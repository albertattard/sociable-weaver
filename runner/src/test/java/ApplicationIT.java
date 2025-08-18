import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/* TODO: Test both artefacts */
class ApplicationIT {

    @Test
    void printsHelp() throws IOException, InterruptedException {
        final File outputFile = newOutputFile();

        final ProcessBuilder processBuilder = processBuilder(outputFile, "--help");

        final Process process = processBuilder.start();
        final int exitCode = process.waitFor();
        assertThat(exitCode)
                .describedAs("The program should exit without error")
                .isEqualTo(0);

        final String output = Files.readString(outputFile.toPath());
        assertThat(output)
                .describedAs("The program should print the help message to the standard output")
                .contains("Sociable Weaver");
    }

    @Test
    void runPlaybookAndProduceOutput() throws IOException, InterruptedException {
        final File outputFile = newOutputFile();

        final ProcessBuilder processBuilder = processBuilder(outputFile,
                "--playbook", "./src/test/resources/fixtures/a.json",
                "--output", "./target/a.md");

        final Process process = processBuilder.start();
        final int exitCode = process.waitFor();
        assertThat(exitCode)
                .describedAs("The program should exit without error")
                .isEqualTo(0);

        final String expected = Files.readString(Path.of("./src/test/resources/fixtures/a.md"));
        final String actual = Files.readString(Path.of("./target/a.md"));
        assertThat(actual)
                .describedAs("The program should produce the expected output")
                .isEqualTo(expected);
    }

    private static ProcessBuilder processBuilder(final File outputFile, final String... args) {
        final String[] command = Stream.concat(
                        /* TODO: Test both artefacts */
                        // Stream.of("java", "-jar", "./target/sw.jar"),
                        Stream.of("./target/sw"),
                        Arrays.stream(args))
                .toArray(String[]::new);

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(outputFile);
        return processBuilder;
    }

    private static File newOutputFile() {
        for (int i = 1; i < 1000; i++) {
            final String fileName = "output-%04d.txt".formatted(i);
            final File outputFile = new File("target", fileName);

            if (outputFile.exists()) {
                continue;
            }

            outputFile.deleteOnExit();
            return outputFile;
        }

        throw new RuntimeException("Failed to find a non-existent file with the pattern 'output-####.txt'");
    }
}
