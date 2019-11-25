package com.dominiccobo.fyp.langserver;

import com.dominiccobo.fyp.langserver.handlers.ExpertsRequestHandler;
import com.dominiccobo.fyp.langserver.handlers.WorkItemsRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfiguration {

    @Bean
    public HandlerMapping handlerMapping(QueryGateway queryGateway, ObjectMapper objectMapper) {
        Map<String, WebSocketHandler> handlerMap = new HashMap<>();
        handlerMap.put(WorkItemsRequestHandler.ENDPOINT_PATH, new WorkItemsRequestHandler(queryGateway, objectMapper));
        handlerMap.put(ExpertsRequestHandler.ENDPOINT_PATH, new ExpertsRequestHandler(queryGateway, objectMapper));
        int beanLoadOrder = -1;
        return new SimpleUrlHandlerMapping(handlerMap, beanLoadOrder);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }
}
