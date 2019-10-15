package com.dominiccobo.fyp.langserver;

import com.dominiccobo.fyp.api.queries.GetWebAppForUpstreamUrl;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.eclipse.lsp4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * TODO: add class description.
 *
 * @author Dominic Cobo (contact@dominiccobo.com)
 */
@Component
public class CustomTextDocumentService implements org.eclipse.lsp4j.services.TextDocumentService {

    private static Logger LOG = LoggerFactory.getLogger(CustomTextDocumentService.class);

    private final QueryGateway queryGateway;

    private TextDocumentItem currentDocument;
    private String gitUpstream;

    @Autowired
    public CustomTextDocumentService(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }


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


        Hover hover = new Hover();
        MarkupContent markedString = new MarkupContent();

        return CompletableFuture.supplyAsync(() -> {
            GetWebAppForUpstreamUrl queryPayload = new GetWebAppForUpstreamUrl(gitUpstream);
            CompletableFuture<GetWebAppForUpstreamUrl.Result> query = queryGateway.query(queryPayload, ResponseTypes.instanceOf(GetWebAppForUpstreamUrl.Result.class));
            query.thenAcceptAsync(new Consumer<GetWebAppForUpstreamUrl.Result>() {

                // FIXME: this needs to be looked over by tonight ....
                @Override
                public void accept(GetWebAppForUpstreamUrl.Result result) {
                    String webAppUrl = result.webAppUrl;
                    LOG.info("Web app url for {} was {}", gitUpstream, webAppUrl);
                    markedString.setValue(webAppUrl);
                }
            });
            return hover;
        });
    }


    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // TODO: stub
    }
}
