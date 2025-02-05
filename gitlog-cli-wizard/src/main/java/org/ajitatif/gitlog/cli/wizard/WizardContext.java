package org.ajitatif.gitlog.cli.wizard;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Component
public class WizardContext {

    private Path currentDirectory = Path.of(System.getProperty("user.dir"));
    private final Set<Path> selectedPaths = new HashSet<>();

    public void setCurrentDirectory(Path currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public Set<Path> getSelectedPaths() {
        return selectedPaths;
    }

    public List<Path> getCurrentSubDirectories() {
        File[] listedFiles = currentDirectory.toFile().listFiles();
        if (listedFiles == null) {
            return new ArrayList<>();
        }
        List<Path> list = new ArrayList<>(Arrays.stream(listedFiles).filter(File::isDirectory).map(File::toPath).toList());
        list.sort(Comparator.naturalOrder());
        return list;
    }

}
