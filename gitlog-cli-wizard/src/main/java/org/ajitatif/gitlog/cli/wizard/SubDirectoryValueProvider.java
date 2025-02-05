package org.ajitatif.gitlog.cli.wizard;

import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class SubDirectoryValueProvider implements ValueProvider {

    private final WizardContext wizardContext;

    public SubDirectoryValueProvider(WizardContext wizardContext) {
        this.wizardContext = wizardContext;
    }

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        final List<Path> allSubdirs = wizardContext.getCurrentSubDirectories();
        List<Path> result = allSubdirs;
        if (!completionContext.currentWord().isBlank()) {
            result = allSubdirs.stream().filter(
                    path -> path.getFileName().startsWith(completionContext.currentWord())).toList();
        }
        return result.stream().map(
                path -> new CompletionProposal(path.getFileName().toString())).toList();

    }

}
