package com.dominiccobo.fyp.langserver.cmd;

import com.dominiccobo.fyp.context.api.queries.AssociatedWorkItemsQuery;
import com.dominiccobo.fyp.context.models.QueryContext;
import com.dominiccobo.fyp.context.models.WorkItem;
import com.dominiccobo.fyp.context.models.git.GitContext;
import com.dominiccobo.fyp.context.models.git.GitRemoteIdentifier;
import com.dominiccobo.fyp.context.models.git.GitRemoteURL;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
public class WorkItemsHttpInterface {

    private final QueryGateway queryGateway;

    @Autowired
    public WorkItemsHttpInterface(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/workItems")
    public List<WorkItem> getWorkItems(@RequestBody QueryContext queryContext) {
        AssociatedWorkItemsQuery associatedWorkItemsQuery = new AssociatedWorkItemsQuery(queryContext);
        ResponseType<List<WorkItem>> responseType = ResponseTypes.multipleInstancesOf(WorkItem.class);
        return queryGateway
                .scatterGather(associatedWorkItemsQuery, responseType, 30, TimeUnit.SECONDS)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @GetMapping("/workItems/remote")
    public List<WorkItem> getWorkItems(@RequestParam("remoteUrl") String remoteUrl) {
        AssociatedWorkItemsQuery qry = queryItemForSingleUpstream(remoteUrl);
        ResponseType<List<WorkItem>> responseType = ResponseTypes.multipleInstancesOf(WorkItem.class);
        return queryGateway
                .scatterGather(qry, responseType, 30, TimeUnit.SECONDS)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private AssociatedWorkItemsQuery queryItemForSingleUpstream(String upstreamUrl) {
        Map<GitRemoteIdentifier, GitRemoteURL> remotes = new HashMap<>();
        remotes.put(new GitRemoteIdentifier("upstream"), new GitRemoteURL(upstreamUrl));
        GitContext gitContext = new GitContext(remotes, null);
        QueryContext queryContext = new QueryContext(gitContext, null);
        return new AssociatedWorkItemsQuery(queryContext);
    }

}
