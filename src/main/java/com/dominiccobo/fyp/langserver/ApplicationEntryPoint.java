package com.dominiccobo.fyp.langserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Application Entry Point
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
@SpringBootApplication
public class ApplicationEntryPoint {

    private static Logger LOG = LoggerFactory.getLogger(ApplicationEntryPoint.class);
    private static final int DEFAULT_PORT = 50 * 1000;

    public static void main(String[] args) throws IOException {
        SpringApplication.run(ApplicationEntryPoint.class, args);

    }

    @EventListener(ApplicationReadyEvent.class)
    @Autowired
    public void onApplicationLaunched(LanguageClientConnection.Factory factory) throws IOException {
        LOG.info("Starting up LSP local server on {}", DEFAULT_PORT);
        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);

        while(true) {
            factory.create(serverSocket.accept()).start();
        }
    }


}
