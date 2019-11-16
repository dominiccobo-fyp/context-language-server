package com.dominiccobo.fyp.langserver.lsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;

@ConditionalOnProperty(name = "lspServerEnabled", havingValue = "true")
@Component
public class SocketConnectionLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(SocketConnectionLauncher.class);
    private static final int DEFAULT_PORT = 50 * 1000;

    private final ServerSocket serverSocket;
    private final LanguageClientConnection.Factory factory;
    private State state = State.RUNNING;

    private enum State {
        RUNNING,
        SHUTTING_DOWN
    }

    @Autowired
    public SocketConnectionLauncher(LanguageClientConnection.Factory factory) throws IOException {
        this.factory = factory;
        serverSocket = new ServerSocket(DEFAULT_PORT);
        onApplicationLaunched();
    }

    public void onApplicationLaunched() {
        // this needs a new thread so that it does not block the main thread ....
        new Thread(() -> {
            try {
                LOG.info("Starting up LSP local server on {}", DEFAULT_PORT);
                factory.create(serverSocket.accept()).start();
            } catch (IOException e) {
                LOG.error("Could not create a new language client:", e);
            }


        }).start();
    }

    @PreDestroy
    public void close() {
        LOG.info("Shutting down socket connection.");
        state = State.SHUTTING_DOWN;
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.error("Could not close socket connection: ", e);
        }
    }
}
