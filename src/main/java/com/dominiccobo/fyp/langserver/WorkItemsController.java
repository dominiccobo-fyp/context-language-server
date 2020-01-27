package com.dominiccobo.fyp.langserver;

import com.dominiccobo.fyp.context.api.queries.AssociatedWorkItemsQuery;
import com.dominiccobo.fyp.context.models.Pagination;
import com.dominiccobo.fyp.context.models.QueryContext;
import com.dominiccobo.fyp.context.models.WorkItem;
import com.dominiccobo.fyp.context.models.document.DocumentContext;
import com.dominiccobo.fyp.context.models.git.GitContext;
import com.dominiccobo.fyp.context.models.git.GitRemoteIdentifier;
import com.dominiccobo.fyp.context.models.git.GitRemoteURL;
import com.dominiccobo.fyp.context.models.git.GitRevision;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class WorkItemsController {

    private static final Logger LOG = LoggerFactory.getLogger(WorkItemsController.class);
    private final QueryGateway queryGateway;
    private final GitFolderRemoteResolver gitFolderRemoteResolver;

    @Autowired
    public WorkItemsController(QueryGateway queryGateway, GitFolderRemoteResolver gitFolderRemoteResolver) {
        this.queryGateway = queryGateway;
        this.gitFolderRemoteResolver = gitFolderRemoteResolver;
    }

    @GetMapping("/workItems")
    public List<WorkItem> get(@RequestParam(required = true) String uri,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int limit) throws IOException, URISyntaxException {

        Pagination pagination = new Pagination(page, limit);
        AssociatedWorkItemsQuery associatedWorkItemsQuery = buildQueryFromFolderUri(uri, pagination);
        ResponseType<List<WorkItem>> responseType = getResponseType();
        return queryGateway
                .scatterGather(associatedWorkItemsQuery, responseType, 60, TimeUnit.SECONDS)
                .flatMap(Collection::stream)
                .peek(workItem -> LOG.trace("Item {}", workItem.getTitle().get()))
                .collect(Collectors.toList());
    }

    private ResponseType<List<WorkItem>> getResponseType() {
        return ResponseTypes.multipleInstancesOf(WorkItem.class);
    }

    private QueryContext buildQueryContext(String folderUri) throws IOException, URISyntaxException {
        DocumentContext documentcontext = buildDocumentContext(folderUri);
        GitContext gitcontext = buildGitContext(folderUri);
        return new QueryContext(gitcontext, documentcontext);
    }

    private GitContext buildGitContext(String folderUri) throws URISyntaxException, IOException {
        GitRevision currentRevision = new GitRevision("", "");

        Map<GitRemoteIdentifier, GitRemoteURL> remotes = gitFolderRemoteResolver.getRemotesForDirectory(new URI(folderUri));
        remotes.forEach(remotes::put);

        return new GitContext(remotes, currentRevision);
    }

    private DocumentContext buildDocumentContext(String folderUri) {
        return new DocumentContext(folderUri);
    }

    private AssociatedWorkItemsQuery buildQueryFromContext(QueryContext queryContext, Pagination pagination) {
        return new AssociatedWorkItemsQuery(queryContext, pagination);
    }

    private AssociatedWorkItemsQuery buildQueryFromFolderUri(String folderUri, Pagination pagination) throws IOException, URISyntaxException {
        return buildQueryFromContext(
                buildQueryContext(folderUri), pagination
        );
    }
}
