package com.dominiccobo.fyp.langserver.handlers;

import com.dominiccobo.fyp.context.models.QueryContext;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.*;
import reactor.core.publisher.UnicastProcessor;

import java.util.*;
import java.util.concurrent.TimeUnit;

abstract class AbstractWebSocketQueryHandler<ReturnType, QueryType> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractWebSocketQueryHandler.class);
    private final QueryGateway queryGateway;
    private final UnicastProcessor<ReturnType> publisher;

    AbstractWebSocketQueryHandler(QueryGateway queryGateway, UnicastProcessor<ReturnType> publisher) {
        this.queryGateway = queryGateway;
        this.publisher = publisher;
    }

    public void consume(QueryContext queryContext) {
        LOG.debug("Consuming query context. Sending query.");
        QueryType associatedWorkItemsQuery = buildQueryFromContext(queryContext);
        ResponseType<List<ReturnType>> responseType = getResponseType();
        queryGateway
                .scatterGather(associatedWorkItemsQuery, responseType, 30, TimeUnit.SECONDS)
                .flatMap(Collection::stream)
                .forEach(publisher::onNext);
    }

    abstract ResponseType<List<ReturnType>> getResponseType();

    abstract QueryType buildQueryFromContext(QueryContext queryContext);
}
