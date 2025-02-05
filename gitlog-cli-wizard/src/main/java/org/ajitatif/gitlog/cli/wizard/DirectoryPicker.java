package org.ajitatif.gitlog.cli.wizard;

import jakarta.validation.constraints.NotNull;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@ShellComponent
public class DirectoryPicker {
    
    private final WizardContext wizardContext;
    
    public DirectoryPicker(WizardContext wizardContext) {
        this.wizardContext = wizardContext;
    }

    @ShellMethod(key = {"pwd" }, value = "Prints the current working directory")
    public String printWorkingDirectory() {
        return wizardContext.getCurrentDirectory().toAbsolutePath().toString();
    }

    @ShellMethod(key = {"cd"}, value = "Change the working directory")
    public String changeDirectory(@ShellOption(valueProvider = SubDirectoryValueProvider.class) String dir) {

        if (wizardContext.getCurrentDirectory().resolve(dir).toFile().exists()) {
            wizardContext.setCurrentDirectory(wizardContext.getCurrentDirectory().resolve(dir).normalize());
            return "Set current directory to " + wizardContext.getCurrentDirectory().toAbsolutePath();
        } else {
            return "Directory does not exist";
        }
    }

    @ShellMethod(key = { "list", "ls", "l" }, value = "List directories to pick in the current directory.")
    public String listDirectories() {

        List<Path> subDirectories = wizardContext.getCurrentSubDirectories();
        if (subDirectories.isEmpty()) return "No directories here.";
        StringBuilder stringBuilder = new StringBuilder();
        subDirectories.stream().map(Path::getFileName).map(directoryName -> directoryName + "\n").forEach(stringBuilder::append);
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    @ShellMethod(key = { "add", "a", }, value = "Add a directory to log export")
    public String addDirectory(@NotNull @ShellOption(valueProvider = SubDirectoryValueProvider.class) String dir) {
        Path path = resolvePath(dir);
        if (!path.toFile().exists()) {
            return "Path " + dir + " does not exist.";
        }
        return wizardContext.getSelectedPaths().add(path) ? "Added " + dir + " to selected paths." : "Path " + dir + " already selected.";
    }

    @ShellMethod(key = { "remove", "rmdir", "d", "rm" }, value = "Remove directory")
    public String removeDirectory(@NotNull @ShellOption(valueProvider = SelectedDirectoryValueProvider.class) String dir) {
        boolean removed = wizardContext.getSelectedPaths().removeIf(Predicate.isEqual(resolvePath(dir)));
        return removed ? format("Removed {0}", dir) : format("{0} not in list", dir);
    }

    private Path resolvePath(String dir) {
        return Paths.get(wizardContext.getCurrentDirectory().toString(), dir);
    }

    @ShellMethod(key = { "clear-selection", "clr" }, value = "Clears the selected directories")
    public String clearSelection() {
        wizardContext.getSelectedPaths().clear();
        return "Clear!";
    }

    @ShellMethod(key = { "which", "picked", "sp" }, value = "Shows currently picked directories")
    public String printPickedDirectories() {
        return wizardContext.getSelectedPaths().stream().map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.joining("\n"));
    }

}
