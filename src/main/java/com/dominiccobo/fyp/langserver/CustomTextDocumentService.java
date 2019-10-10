package com.dominiccobo.fyp.langserver;

import org.eclipse.lsp4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
@Component
public class CustomTextDocumentService implements org.eclipse.lsp4j.services.TextDocumentService {

    private static Logger LOG = LoggerFactory.getLogger(CustomTextDocumentService.class);

    private TextDocumentItem currentDocument;
    private String gitUpstream;

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LOG.debug("Opened document {}", params.getTextDocument().getUri());
        currentDocument = params.getTextDocument();
        try {
            gitUpstream = GitUtils.getUpstreamForFile(new URI(params.getTextDocument().getUri()));
        } catch (IOException | URISyntaxException e) {
            LOG.error("Could not get git upstream.", e);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        currentDocument = null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        final String docUri = position.getTextDocument().getUri();

        try {
            gitUpstream = GitUtils.getUpstreamForFile(new URI(docUri));
        } catch (IOException | URISyntaxException e) {
            LOG.error("Could not get git upstream.", e);
        }

        return CompletableFuture.supplyAsync(() -> new Hover(new MarkupContent("markdown", "__Upstream VCS__:\n\n``" + gitUpstream + "``")));
    }


    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // TODO: stub
    }
}
