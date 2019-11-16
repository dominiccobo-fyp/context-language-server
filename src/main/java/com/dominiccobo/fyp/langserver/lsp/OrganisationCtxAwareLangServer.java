package com.dominiccobo.fyp.langserver.lsp;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
public class OrganisationCtxAwareLangServer implements LanguageServer {

    private final CustomTextDocumentService customTextDocumentService;
    private final WorkspaceService workspaceService;

    public OrganisationCtxAwareLangServer(
            CustomTextDocumentService customTextDocumentService,
            CustomWorkspaceService workspaceService
    ) {
        this.customTextDocumentService = customTextDocumentService;
        this.workspaceService = workspaceService;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

        InitializeResult initialisedResults = new InitializeResult(new ServerCapabilities());
        initialisedResults.getCapabilities().setHoverProvider(Boolean.TRUE);
        return CompletableFuture.supplyAsync(() -> initialisedResults);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.supplyAsync(() -> Boolean.TRUE);
    }

    @Override
    public void exit() {

    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.customTextDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }
}
