package demo.cli;

import org.apache.commons.cli.*;

import java.nio.file.Path;

public record CommandLineArguments(boolean showHelp, Path playbook, Path output) {

    public static CommandLineArguments parse(final String[] args) {
        try {
            final Options options = options();

            final CommandLineParser parser = new DefaultParser();
            final CommandLine commandLine = parser.parse(options, args);

            final boolean showHelp = parseHelp(commandLine);
            final Path playbook = parsePlaybook(commandLine);
            final Path output = parseOutput(commandLine);

            return new CommandLineArguments(showHelp, playbook, output);
        } catch (final ParseException e) {
            throw new RuntimeException("Failed to parse the command line arguments", e);
        }
    }

    public void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("sw", options());
    }

    private static boolean parseHelp(final CommandLine commandLine) throws ParseException {
        return commandLine.hasOption(HELP_OPTION);
    }

    private static Path parsePlaybook(final CommandLine commandLine) throws ParseException {
        return commandLine.hasOption(PLAYBOOK_OPTION)
                ? commandLine.getParsedOptionValue(PLAYBOOK_OPTION)
                : Path.of("sw-runbook.json");
    }

    private static Path parseOutput(final CommandLine commandLine) throws ParseException {
        return commandLine.hasOption(OUTPUT_OPTION)
                ? commandLine.getParsedOptionValue(OUTPUT_OPTION)
                : Path.of("README.md");
    }

    private static Options options() {
        final Options options = new Options();
        options.addOption(HELP_OPTION);
        options.addOption(PLAYBOOK_OPTION);
        options.addOption(OUTPUT_OPTION);
        return options;
    }

    private static final Option HELP_OPTION = Option.builder()
            .required(false)
            .option("h")
            .longOpt("help")
            .desc("Prints the help")
            .build();

    private static final Option PLAYBOOK_OPTION = Option.builder()
            .required(false)
            .option("f")
            .longOpt("playbook")
            .hasArg(true)
            .numberOfArgs(1)
            .converter(Path::of)
            .desc("The path to the Sociable Weaver playbook, default 'sw-playbook.json'")
            .build();

    private static final Option OUTPUT_OPTION = Option.builder()
            .required(false)
            .option("o")
            .longOpt("output")
            .hasArg(true)
            .numberOfArgs(1)
            .converter(Path::of)
            .desc("The path to the Markdown output file, default 'README.md'")
            .build();
}
