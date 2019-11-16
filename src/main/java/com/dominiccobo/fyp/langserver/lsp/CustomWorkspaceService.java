package com.dominiccobo.fyp.langserver.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
@ConditionalOnProperty(name = "lspServerEnabled", havingValue = "true")
@Component
public class CustomWorkspaceService implements org.eclipse.lsp4j.services.WorkspaceService {
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // TODO: stub
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO: stub
    }
}
