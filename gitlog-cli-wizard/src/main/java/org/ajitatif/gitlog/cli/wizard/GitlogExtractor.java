package org.ajitatif.gitlog.cli.wizard;

import org.ajitatif.gitlog.converter.GitLogConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.text.MessageFormat.format;

@ShellComponent
public class GitlogExtractor {

    private final WizardContext wizardContext;

    public GitlogExtractor(WizardContext wizardContext) {
        this.wizardContext = wizardContext;
    }

    @ShellMethod(key = {"extract", "x" }, value = "Extracts git logs, using command git")
    public String extractGitlogs(@ShellOption(value = "from-date", help = "Start date of gitlog extraction. Formatted as YYYY-MM-dd") String formattedDate,
                                 @ShellOption(value = "outfile", help="Output file, relative to working directory") String outfile) {
        Set<Path> selectedPaths = wizardContext.getSelectedPaths();
        if (selectedPaths.isEmpty()) {
            return "No directories selected for git log extraction. Select some first.";
        }
        try {
            DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(formattedDate);
        } catch (Exception e) {
            return "The given date doesn't seem to match the pattern YYYY-mm-dd";
        }
        List<String> logMap = new LinkedList<>();
        for (Path path : selectedPaths) {
            logMap.addAll(extractGitLogsForDirectory(path, formattedDate));
        }

        logMap.addFirst( "JIRA Key;Author;Service");
        // write all logMap into a new file
        Path outfilePath = wizardContext.getCurrentDirectory().resolve(outfile);
        if (outfilePath.toFile().exists()) {
            Scanner scanner = new Scanner(System.in);
            System.out.print(format("File {0} exists. Overwrite? (y/n): ", outfilePath));
            String answer = scanner.nextLine();
            if (!answer.equalsIgnoreCase("y")) {
                return "Aborting, no changes made.";
            }
            System.out.println("Kill it!");
        }
        try {
            Files.write(outfilePath, logMap, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return format("Done, the file available at {0}", outfilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new CommandExecutionException(format("Cannot write to file {0}", outfilePath), e);
        }

    }

    private List<String> extractGitLogsForDirectory(Path directory, String formattedDate) {
        // git log --pretty=format:"%s|%al" --date=short --after=YYYY-MM-DD > /path/to/logfile
        ProcessBuilder processBuilder = new ProcessBuilder(List.of("git", "log",
                "--pretty=format:%s|%al", "--date=short", format("--after={0}", formattedDate)));
        processBuilder.directory(directory.toFile());
        try {
            Process process = processBuilder.start();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            process.getInputStream().transferTo(out);
            process.waitFor();
            String outString = out.toString(StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                throw new RuntimeException(format("The command git for directory {0} returned with error code {1}\n{2}",
                        directory.toAbsolutePath().toString(), process.exitValue(), outString));
            }
            List<String> lines = Arrays.stream(outString.split(System.lineSeparator())).toList();
            return lines.stream().map(line ->
                    format("{0};{1}",
                            GitLogConverter.convertLogLine(line),
                            directory.getFileName().toString())).toList();
        } catch (Exception e) {
            throw new CommandExecutionException(format("Error while running git command for directory {0}: {1}",
                    directory.toAbsolutePath().toString(), e.getMessage()), e);
        }

    }
}
