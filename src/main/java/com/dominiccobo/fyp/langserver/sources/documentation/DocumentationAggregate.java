package com.dominiccobo.fyp.langserver.sources.documentation;

import com.dominiccobo.fyp.context.api.queries.AssociatedDocumentationQuery;
import com.dominiccobo.fyp.context.models.*;
import com.dominiccobo.fyp.context.models.document.DocumentContext;
import com.dominiccobo.fyp.context.models.git.GitContext;
import com.dominiccobo.fyp.context.models.git.GitRemoteIdentifier;
import com.dominiccobo.fyp.context.models.git.GitRemoteURL;
import com.dominiccobo.fyp.context.models.git.GitRevision;
import com.dominiccobo.fyp.langserver.GitFolderRemoteResolver;
import com.dominiccobo.fyp.langserver.sources.SourceConfiguration;
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

public class DocumentationAggregate {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentationAggregate.class);
    private static final int FIRST_PAGE_INDEX = 0;

    private final String identifier = UUID.randomUUID().toString();
    private final ArrayList<StreamTracker<Documentation>> inputs = new ArrayList<>();
    private final QueryGateway queryGateway;
    private final GitFolderRemoteResolver gitFolderRemoteResolver;
    private final String queryUri;
    private final String searchTerm;
    private final DocumentationType documentationType;
    private final SourceConfiguration config;

    public DocumentationAggregate(QueryGateway queryGateway, GitFolderRemoteResolver gitFolderRemoteResolver,
                                  DocumentationRequestContext documentationRequestContext, SourceConfiguration config) {
        this.queryGateway = queryGateway;
        this.gitFolderRemoteResolver = gitFolderRemoteResolver;
        this.queryUri = documentationRequestContext.getQueryUri();
        this.searchTerm = documentationRequestContext.getSearchTerm();
        this.documentationType = documentationRequestContext.getDocumentationType();
        this.config = config;
    }

    public void run() throws IOException, URISyntaxException {
        LOG.info("Running query for {}: {}: {}", queryUri, searchTerm, documentationType);

        int page = FIRST_PAGE_INDEX;
        int limit = config.getDocumentation().getDefaultAggregateBufferPageSize();
        int maxTotalRetrievedItems = config.getDocumentation().getAggregateBufferMaximumItems();

        while ((page * limit) < maxTotalRetrievedItems) {
            LOG.debug("Fetching chunked pages for q={} uri={} (page: {} - page size: {})", searchTerm, queryUri, page, limit);
            Pagination pagination = new Pagination(page, limit);
            AssociatedDocumentationQuery associatedWorkItemsQuery = buildQueryFromFolderUri(this.queryUri, pagination);
            ResponseType<List<Documentation>> responseType = getResponseType();

            queryGateway
                    .scatterGather(associatedWorkItemsQuery, responseType, config.getDocumentation().getDefaultQueryTimeoutSeconds(), TimeUnit.SECONDS)
                    .forEach(workItemSource -> {
                        this.addInput(workItemSource.stream());
                    });

            page++;
        }
    }

    private ResponseType<List<Documentation>> getResponseType() {
        return ResponseTypes.multipleInstancesOf(Documentation.class);
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

    private AssociatedDocumentationQuery buildQueryFromContext(QueryContext queryContext, Pagination pagination) {
        return new AssociatedDocumentationQuery.Builder(queryContext, pagination)
                .forDocumentationType(documentationType)
                .forSearchTerm(searchTerm)
                .build();
    }

    private AssociatedDocumentationQuery buildQueryFromFolderUri(String folderUri, Pagination pagination) throws IOException, URISyntaxException {
        return buildQueryFromContext(
                buildQueryContext(folderUri), pagination
        );
    }

    public void addInput(Stream<Documentation> result) {
        inputs.add(new StreamTracker<>(result));
    }

    public List<Documentation> getResults() {
        ArrayList<Documentation> results = new ArrayList<>();

        this.inputs.forEach(
                workItemStreamTracker -> {
                    results.addAll(workItemStreamTracker.getItems());
                }
        );
        return results;
    }

    public String getIdentifier() {
        return identifier;
    }

    static class DocumentationRequestContext {
        private final String queryUri;
        private final String searchTerm;
        private final DocumentationType documentationType;

        DocumentationRequestContext(String queryUri, String searchTerm, DocumentationType documentationType) {
            this.queryUri = queryUri;
            this.searchTerm = searchTerm;
            this.documentationType = documentationType;
        }

        public final String getQueryUri() {
            return queryUri;
        }

        public final String getSearchTerm() {
            return searchTerm;
        }

        public final DocumentationType getDocumentationType() {
            return documentationType;
        }
    }
}
