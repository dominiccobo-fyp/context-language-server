package com.dominiccobo.fyp.langserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application Entry Point
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
@SpringBootApplication
public class LocalLangServerApp {

    private static Logger LOG = LoggerFactory.getLogger(LocalLangServerApp.class);

    public static void main(String[] args) {
        SpringApplication.run(LocalLangServerApp.class, args);

    }


}
