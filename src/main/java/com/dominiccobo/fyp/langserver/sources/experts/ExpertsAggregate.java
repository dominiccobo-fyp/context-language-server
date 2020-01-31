package com.dominiccobo.fyp.langserver.sources.experts;

import com.dominiccobo.fyp.context.api.queries.AssociatedExpertsQuery;
import com.dominiccobo.fyp.context.models.Expert;
import com.dominiccobo.fyp.context.models.Pagination;
import com.dominiccobo.fyp.context.models.QueryContext;
import com.dominiccobo.fyp.context.models.document.DocumentContext;
import com.dominiccobo.fyp.context.models.git.GitContext;
import com.dominiccobo.fyp.context.models.git.GitRemoteIdentifier;
import com.dominiccobo.fyp.context.models.git.GitRemoteURL;
import com.dominiccobo.fyp.context.models.git.GitRevision;
import com.dominiccobo.fyp.langserver.GitFolderRemoteResolver;
import com.dominiccobo.fyp.langserver.sources.StreamTracker;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ExpertsAggregate {

    private static final Logger LOG = LoggerFactory.getLogger(ExpertsAggregate.class);

    private final String identifier = UUID.randomUUID().toString();
    private final ArrayList<StreamTracker<Expert>> inputs = new ArrayList<>();
    private final QueryGateway queryGateway;
    private final GitFolderRemoteResolver gitFolderRemoteResolver;
    private final String queryUri;

    public ExpertsAggregate(QueryGateway queryGateway, GitFolderRemoteResolver gitFolderRemoteResolver, String queryUri) {
        this.queryGateway = queryGateway;
        this.gitFolderRemoteResolver = gitFolderRemoteResolver;
        this.queryUri = queryUri;
    }

    public void run() throws IOException, URISyntaxException {
        LOG.info("Running query for {}", queryUri);

        int page = 0;
        int limit = 20;

        // FIXME: this is arbitrary and requires proper thoughtful consideration  ...
        int someLimit = 500;

        while ((page * limit) < someLimit) {
            LOG.debug("Fetching chunked pages for uri {} (page: {} - page size: {})", queryUri, page, limit);
            Pagination pagination = new Pagination(page, limit);
            AssociatedExpertsQuery associatedExpertsQuery = buildQueryFromFolderUri(this.queryUri, pagination);
            ResponseType<List<Expert>> responseType = getResponseType();

            queryGateway
                    .scatterGather(associatedExpertsQuery, responseType, 60, TimeUnit.SECONDS)
                    .forEach(ExpertSource -> {
                        this.addInput(ExpertSource.stream());
                    });

            page++;
        }
    }

    private ResponseType<List<Expert>> getResponseType() {
        return ResponseTypes.multipleInstancesOf(Expert.class);
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

    private AssociatedExpertsQuery buildQueryFromContext(QueryContext queryContext, Pagination pagination) {
        return new AssociatedExpertsQuery(queryContext, pagination);
    }

    private AssociatedExpertsQuery buildQueryFromFolderUri(String folderUri, Pagination pagination) throws IOException, URISyntaxException {
        return buildQueryFromContext(
                buildQueryContext(folderUri), pagination
        );
    }

    public void addInput(Stream<Expert> result) {
        inputs.add(new StreamTracker<>(result));
    }

    public List<Expert> getResults() {
        ArrayList<Expert> Experts = new ArrayList<>();

        this.inputs.forEach(
                ExpertStreamTracker -> {
                    Experts.addAll(ExpertStreamTracker.getItems());
                }
        );
        return Experts;
    }

    public String getIdentifier() {
        return identifier;
    }
}
