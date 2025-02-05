package org.ajitatif.gitlog.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitLogConverter {

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.err.println("Need git log filename. For the correct format of the log file, use:\n" +
                    "git log --pretty=format:\"%s|%al\" --date=short --after=YYYY-MM-DD");
            System.exit(-256);
        }

        String lineAppendix = "";
        if (args.length > 1) {
            lineAppendix = args[1];
        }

        String logFile = args[0];
        if (!logFile.isBlank()) {
            if (!Files.exists(Paths.get(logFile))) {
                System.err.println("File" + logFile + " does not seem to exist");
            }
            convertLogFile(logFile, lineAppendix);
        }
    }

    private static void convertLogFile(String logFile, String lineAppendix) throws IOException {

        final List<String> lines = Files.readAllLines(Paths.get(logFile));
        if (lineAppendix.isBlank()) {
            lines.stream().map(GitLogConverter::convertLogLine).forEach(System.out::println);
        } else {
            lines.stream().map(GitLogConverter::convertLogLine).forEach(line -> System.out.println(line + ";" + lineAppendix));
        }
    }

    public static String convertLogLine(String logLine) {
        final String[] fields = logLine.split("[|]");
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            line.append(convertField(fields[i], i)).append(";");
        }
        return line.substring(0, line.length() - 1);
    }

    private static String convertField(String field, int fieldNumber) {

        return switch (fieldNumber) {
            case 0 -> // %s - commit message
                extractJiraTicketKey(field);
            case 1 -> // %al - author local name
                    field.replaceAll("\\d+\\+", "");
            default ->
                "";
        };
    }

    private static String extractJiraTicketKey(String text) {
        // Merge pull request #787 from wkda/REMEX-611_1
        final Matcher matcher = Pattern.compile("([A-Za-z]+-\\d+)|(SPIKE)").matcher(text);
        return matcher.find() ? matcher.group() : text;
    }
}
