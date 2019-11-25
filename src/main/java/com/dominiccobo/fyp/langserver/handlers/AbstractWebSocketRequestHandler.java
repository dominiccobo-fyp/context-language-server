package com.dominiccobo.fyp.langserver.handlers;

import com.dominiccobo.fyp.context.models.QueryContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.*;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.*;

import java.io.IOException;

public abstract class AbstractWebSocketRequestHandler<RequestReturnType, WebSocketHandlerType extends AbstractWebSocketQueryHandler>  implements WebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractWebSocketQueryHandler.class);
    protected final ObjectMapper objectMapper;
    protected final QueryGateway queryGateway;

    public AbstractWebSocketRequestHandler(ObjectMapper objectMapper, QueryGateway queryGateway) {
        this.objectMapper = objectMapper;
        this.queryGateway = queryGateway;
    }

    protected QueryContext toQueryContext(String json) {
        try {
            return objectMapper.readValue(json, QueryContext.class);
        } catch (IOException e) {
            LOG.error("Invalid JSON", e);
            throw new RuntimeException("Invalid JSON:" + json, e);
        }
    }

    abstract String toFormattedMarkdown(RequestReturnType workItem);

    Flux<String> createMarkdownMarshallingReturnFlux(UnicastProcessor<RequestReturnType> response) {
        return Flux.from(response).map(this::toFormattedMarkdown);
    }

    protected Mono<Void> createOutputHandlingStream(WebSocketSession session, Flux<String> outputFlux) {
        return session.send(outputFlux.map(session::textMessage));
    }

    protected Mono<Void> createInputHandlingStream(WebSocketSession session, WebSocketHandlerType queryStreamHandler) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toQueryContext)
                .doOnNext(queryStreamHandler::consume).then();
    }
}
