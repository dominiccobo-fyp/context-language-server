package com.dominiccobo.fyp.langserver;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
public class OrganisationCtxAwareLangServer implements LanguageServer {

    private CustomTextDocumentService customTextDocumentService;
    private WorkspaceService workspaceService;

    public OrganisationCtxAwareLangServer() {
        this.customTextDocumentService = new CustomTextDocumentService();
        this.workspaceService = new CustomWorkspaceService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

        InitializeResult initialisedResults = new InitializeResult(new ServerCapabilities());
        final ServerCapabilities capabilities = initialisedResults.getCapabilities();
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
    public org.eclipse.lsp4j.services.TextDocumentService getTextDocumentService() {
        return this.customTextDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }


}
