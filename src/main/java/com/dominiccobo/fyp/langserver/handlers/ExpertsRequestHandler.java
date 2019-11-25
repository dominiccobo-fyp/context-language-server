package com.dominiccobo.fyp.langserver.handlers;

import com.dominiccobo.fyp.context.api.queries.AssociatedExpertsQuery;
import com.dominiccobo.fyp.context.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.messaging.responsetypes.*;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.*;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.*;

import java.util.List;

public class ExpertsRequestHandler extends AbstractWebSocketRequestHandler<Expert, ExpertsRequestHandler.WebSocketQueryHandler> {

    public static final String TITLE = "Experts";
    public static final String ENDPOINT_PATH = "/experts";

    private static final Logger LOG = LoggerFactory.getLogger(ExpertsRequestHandler.class);

    public ExpertsRequestHandler(QueryGateway queryGateway, ObjectMapper objectMapper) {
        super(objectMapper, queryGateway);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        LOG.info("Received incoming session {}", session.getHandshakeInfo().getRemoteAddress());
        UnicastProcessor<Expert> response = UnicastProcessor.create();
        Flux<String> outputFlux = createMarkdownMarshallingReturnFlux(response);
        WebSocketQueryHandler queryStreamHandler = new WebSocketQueryHandler(queryGateway, response);
        Mono<Void> input = createInputHandlingStream(session, queryStreamHandler);
        Mono<Void> output = createOutputHandlingStream(session, outputFlux);
        return Mono.zip(input, output).then();
    }

    String toFormattedMarkdown(Expert item) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("# %s%n", item.getExpertName()));
        return stringBuilder.toString();
    }

    static class WebSocketQueryHandler extends AbstractWebSocketQueryHandler<Expert, AssociatedExpertsQuery>{
        private WebSocketQueryHandler(QueryGateway queryGateway, UnicastProcessor<Expert> publisher) {
            super(queryGateway, publisher);
        }

        @Override
        ResponseType<List<Expert>> getResponseType() {
            return ResponseTypes.multipleInstancesOf(Expert.class);
        }

        @Override
        AssociatedExpertsQuery buildQueryFromContext(QueryContext queryContext) {
            return new AssociatedExpertsQuery(queryContext);
        }
    }
}
