package com.dominiccobo.fyp.langserver.sources.experts;

import com.dominiccobo.fyp.context.models.Expert;
import com.dominiccobo.fyp.langserver.GitFolderRemoteResolver;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class ExpertsAggregateService {

    private final QueryGateway queryGateway;
    private final GitFolderRemoteResolver gitFolderRemoteResolver;
    private final Map<String, ExpertsAggregate> Experts = new HashMap<>();

    @Autowired
    public ExpertsAggregateService(QueryGateway queryGateway, GitFolderRemoteResolver gitFolderRemoteResolver) {
        this.queryGateway = queryGateway;
        this.gitFolderRemoteResolver = gitFolderRemoteResolver;
    }

    public ExpertsAggregate createExpertsAggregate(String uri) throws IOException, URISyntaxException {
        ExpertsAggregate createdAggregate = new ExpertsAggregate(queryGateway, gitFolderRemoteResolver, uri);
        Experts.put(createdAggregate.getIdentifier(), createdAggregate);
        return createdAggregate;
    }

    @Async
    public void run(ExpertsAggregate aggregate) throws IOException, URISyntaxException {
        aggregate.run();
    }

    public List<Expert> queryForExperts(String sessionIdentifier) throws IOException, URISyntaxException {
        Optional<ExpertsAggregate> nullableReturn = Optional.ofNullable(Experts.get(sessionIdentifier));
        if (nullableReturn.isPresent()) {
            return nullableReturn.get().getResults();
        }
        else {
            return new ArrayList<>();
        }
    }
}
