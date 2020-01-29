package com.dominiccobo.fyp.langserver.aggregation;

import com.dominiccobo.fyp.context.models.WorkItem;
import com.dominiccobo.fyp.langserver.GitFolderRemoteResolver;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AggregateService {

    private final QueryGateway queryGateway;
    private final GitFolderRemoteResolver gitFolderRemoteResolver;
    private final Map<String, WorkItemsAggregate> workItems = new HashMap<>();

    @Autowired
    public AggregateService(QueryGateway queryGateway, GitFolderRemoteResolver gitFolderRemoteResolver) {
        this.queryGateway = queryGateway;
        this.gitFolderRemoteResolver = gitFolderRemoteResolver;
    }

    public WorkItemsAggregate createWorkItemsAggregate(String uri) throws IOException, URISyntaxException {
        WorkItemsAggregate createdAggregate = new WorkItemsAggregate(queryGateway, gitFolderRemoteResolver, uri);
        workItems.put(createdAggregate.getIdentifier(), createdAggregate);
        return createdAggregate;
    }

    @Async
    public void run(WorkItemsAggregate aggregate) throws IOException, URISyntaxException {
        aggregate.run();
    }

    public List<WorkItem> queryForWorkItems(String sessionIdentifier) throws IOException, URISyntaxException {
        return workItems.get(sessionIdentifier).getResults();
    }
}
