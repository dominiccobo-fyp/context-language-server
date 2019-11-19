package com.dominiccobo.fyp.langserver;

import com.dominiccobo.fyp.langserver.cmd.WorkItemsRequestHandler;
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
    public HandlerMapping handlerMapping(QueryGateway queryGateway) {
        Map<String, WebSocketHandler> handlerMap = new HashMap<>();
        handlerMap.put("/workItems", new WorkItemsRequestHandler(queryGateway));
        int beanLoadOrder = -1;
        return new SimpleUrlHandlerMapping(handlerMap, beanLoadOrder);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
