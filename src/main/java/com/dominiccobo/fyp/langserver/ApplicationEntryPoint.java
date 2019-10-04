package com.dominiccobo.fyp.langserver;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
public class ApplicationEntryPoint {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final OrganisationCtxAwareLangServer organisationCtxAwareLangServer = new OrganisationCtxAwareLangServer();
        final InitializeParams params = new InitializeParams();
        organisationCtxAwareLangServer.initialize(params);
        final Launcher<LanguageClient> serverLauncher = LSPLauncher.createServerLauncher(organisationCtxAwareLangServer, System.in, System.out);
        final Future<Void> isListening = serverLauncher.startListening();
        isListening.get();
    }
}
