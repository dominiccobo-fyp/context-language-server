package com.dominiccobo.fyp.langserver.cmd;

import com.dominiccobo.fyp.context.api.queries.AssociatedWorkItemsQuery;
import com.dominiccobo.fyp.context.models.QueryContext;
import com.dominiccobo.fyp.context.models.WorkItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WorkItemsRequestHandler implements WebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WorkItemsRequestHandler.class);

    private final ObjectMapper objectMapper;
    private final QueryGateway queryGateway;

    public WorkItemsRequestHandler(QueryGateway queryGateway) {

        this.queryGateway = queryGateway;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new Jdk8Module());
    }


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        LOG.info("Received incoming session {}", session.getHandshakeInfo().getRemoteAddress());
        UnicastProcessor<WorkItem> response = UnicastProcessor.create();
        Flux<String> outputFlux = Flux.from(response).map(this::toJSON);

        WebSocketQueryHandler queryStreamHandler = new WebSocketQueryHandler(queryGateway, response);
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toQueryContext)
                .doOnNext(queryStreamHandler::consume).then();

        Mono<Void> output = session.send(outputFlux.map(session::textMessage));
        return Mono.zip(input, output).then();
    }

    static class WebSocketQueryHandler {
        private final QueryGateway queryGateway;
        private final UnicastProcessor<WorkItem> publisher;

        WebSocketQueryHandler(QueryGateway queryGateway, UnicastProcessor<WorkItem> publisher) {
            this.queryGateway = queryGateway;
            this.publisher = publisher;
        }

        void consume(QueryContext queryContext) {
            LOG.info("Handling query context");
            AssociatedWorkItemsQuery associatedWorkItemsQuery = new AssociatedWorkItemsQuery(queryContext);
            ResponseType<List<WorkItem>> responseType = ResponseTypes.multipleInstancesOf(WorkItem.class);
            queryGateway
                    .scatterGather(associatedWorkItemsQuery, responseType, 30, TimeUnit.SECONDS)
                    .flatMap(Collection::stream)
                    .forEach(t -> {
                        LOG.info("Retrieved {}", t.getTitle());
                        publisher.onNext(t);
                    });
        }
    }

    private QueryContext toQueryContext(String json) {
        try {
            return objectMapper.readValue(json, QueryContext.class);
        } catch (IOException e) {
            LOG.error("Invalid JSON", e);
            throw new RuntimeException("Invalid JSON:" + json, e);
        }
    }

    private String toJSON(WorkItem workItem) {
        try {
            return objectMapper.writeValueAsString(workItem);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}