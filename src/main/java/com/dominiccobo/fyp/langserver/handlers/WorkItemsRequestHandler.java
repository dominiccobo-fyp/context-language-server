package com.dominiccobo.fyp.langserver.handlers;

import com.dominiccobo.fyp.context.api.queries.AssociatedWorkItemsQuery;
import com.dominiccobo.fyp.context.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.messaging.responsetypes.*;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.*;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.*;
import java.util.List;

public class WorkItemsRequestHandler extends AbstractWebSocketRequestHandler<WorkItem, WorkItemsRequestHandler.WebSocketQueryHandler> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkItemsRequestHandler.class);

    public static final String TITLE = "Work Items";
    public static final String ENDPOINT_PATH = "/workItems";

    public WorkItemsRequestHandler(QueryGateway queryGateway, ObjectMapper objectMapper) {
        super(objectMapper, queryGateway);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        LOG.info("Received incoming session {}", session.getHandshakeInfo().getRemoteAddress());
        UnicastProcessor<WorkItem> response = UnicastProcessor.create();
        Flux<String> outputFlux = createMarkdownMarshallingReturnFlux(response);
        WebSocketQueryHandler queryStreamHandler = new WebSocketQueryHandler(queryGateway, response);
        Mono<Void> input = createInputHandlingStream(session, queryStreamHandler);
        Mono<Void> output = createOutputHandlingStream(session, outputFlux);
        return Mono.zip(input, output).then();
    }

    protected String toFormattedMarkdown(WorkItem workItem) {
        StringBuilder stringBuilder = new StringBuilder();
        // FIXME:
        stringBuilder.append(String.format("# %s%n%n", workItem.getTitle().get()));
        stringBuilder.append(String.format("%s", workItem.getBody().get()));
        return stringBuilder.toString();
    }

    static class WebSocketQueryHandler extends AbstractWebSocketQueryHandler<WorkItem, AssociatedWorkItemsQuery> {

        private WebSocketQueryHandler(QueryGateway queryGateway, UnicastProcessor<WorkItem> publisher) {
            super(queryGateway, publisher);
        }

        @Override
        ResponseType<List<WorkItem>> getResponseType() {
            return ResponseTypes.multipleInstancesOf(WorkItem.class);
        }

        @Override
        AssociatedWorkItemsQuery buildQueryFromContext(QueryContext queryContext) {
            return new AssociatedWorkItemsQuery(queryContext);
        }
    }

}