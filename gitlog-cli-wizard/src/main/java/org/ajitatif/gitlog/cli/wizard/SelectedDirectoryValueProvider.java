package org.ajitatif.gitlog.cli.wizard;

import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelectedDirectoryValueProvider implements ValueProvider {

    private final WizardContext wizardContext;

    public SelectedDirectoryValueProvider(WizardContext wizardContext) {
        this.wizardContext = wizardContext;
    }

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        return wizardContext.getSelectedPaths().stream().map(path ->
                new CompletionProposal(path.toAbsolutePath().toString())).toList();
    }
}
