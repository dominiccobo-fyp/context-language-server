package com.dominiccobo.fyp.langserver.lsp;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.concurrent.Future;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
public class LanguageClientConnection extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(LanguageClientConnection.class);

    private final Socket socket;
    private final LanguageServer server;

    @ConditionalOnProperty(name = "lspServerEnabled", havingValue = "true")
    @Component
    public static class Factory {

        private final CustomWorkspaceService workspaceService;
        private final CustomTextDocumentService textDocumentService;

        @Autowired
        public Factory(CustomWorkspaceService workspaceService, CustomTextDocumentService textDocumentService) {
            this.workspaceService = workspaceService;
            this.textDocumentService = textDocumentService;
        }

        public LanguageClientConnection create(Socket socket) {
            return new LanguageClientConnection(socket, workspaceService, textDocumentService);
        }
    }

    protected LanguageClientConnection(Socket socket, CustomWorkspaceService workspaceService,
                                    CustomTextDocumentService textDocumentService) {
        this.socket = socket;
        this.server = new OrganisationCtxAwareLangServer(textDocumentService, workspaceService);
    }

    @Override
    public void run() {
        LOG.info("Handling connection from client {}", socket.getInetAddress().toString());
        try {
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
                    server, socket.getInputStream(), socket.getOutputStream()
            );
            Future<Void> isListening = launcher.startListening();
            isListening.get();
        }
        catch (Exception e) {
            LOG.error("Could not accept connection from {}", socket.getInetAddress().toString(), e);
        }
    }
}
