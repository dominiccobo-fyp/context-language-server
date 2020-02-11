package com.dominiccobo.fyp.langserver.sources.documentation;

import com.dominiccobo.fyp.context.models.Documentation;
import com.dominiccobo.fyp.langserver.GitFolderRemoteResolver;
import com.dominiccobo.fyp.langserver.sources.SourceConfiguration;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class DocumentationAggregateService {

    private final QueryGateway queryGateway;
    private final GitFolderRemoteResolver gitFolderRemoteResolver;
    private final Map<String, DocumentationAggregate> documentationItems = new HashMap<>();
    private final SourceConfiguration config;

    @Autowired
    public DocumentationAggregateService(QueryGateway queryGateway, GitFolderRemoteResolver gitFolderRemoteResolver, SourceConfiguration config) {
        this.queryGateway = queryGateway;
        this.gitFolderRemoteResolver = gitFolderRemoteResolver;
        this.config = config;
    }

    public DocumentationAggregate createAggregate(DocumentationAggregate.DocumentationRequestContext documentationRequestContext) {
        DocumentationAggregate createdAggregate = new DocumentationAggregate(queryGateway, gitFolderRemoteResolver, documentationRequestContext, config);
        documentationItems.put(createdAggregate.getIdentifier(), createdAggregate);
        return createdAggregate;
    }

    @Async
    public void run(DocumentationAggregate aggregate) throws IOException, URISyntaxException {
        aggregate.run();
    }

    public List<Documentation> queryForItems(String sessionIdentifier) {
        Optional<DocumentationAggregate> nullableReturn = Optional.ofNullable(documentationItems.get(sessionIdentifier));
        if (nullableReturn.isPresent()) {
            return nullableReturn.get().getResults();
        }
        else {
            return new ArrayList<>();
        }
    }
}
