package com.dominiccobo.fyp.langserver;

import com.dominiccobo.fyp.context.api.events.GitRemoteURLRecognisedEvent;
import com.dominiccobo.fyp.context.models.git.GitRemoteIdentifier;
import com.dominiccobo.fyp.context.models.git.GitRemoteURL;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController()
public class CacheUpdateRequestEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(CacheUpdateRequestEndpoint.class);

    private final GitFolderRemoteResolver gitFolderRemoteResolver;
    private final EventGateway eventGateway;

    @Autowired
    public CacheUpdateRequestEndpoint(GitFolderRemoteResolver gitFolderRemoteResolver, EventGateway eventGateway) {
        this.gitFolderRemoteResolver = gitFolderRemoteResolver;
        this.eventGateway = eventGateway;
    }

    @PutMapping(value = "/workspace/cache/upstream", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateCache(@RequestBody CacheUpdateRequest request) throws URISyntaxException, IOException {
        Optional<ArrayList<String>> urls = request.getFolderUris();
        if (urls.isPresent()) {
            for(String uri: urls.get()) {
                Map<GitRemoteIdentifier, GitRemoteURL> remotes = gitFolderRemoteResolver.getRemotesForDirectory(new URI(uri));
                remotes.forEach((key, value) -> {
                    this.eventGateway.publish(new GitRemoteURLRecognisedEvent(value));
                    LOG.info("{}: {}", uri, value.getUrl());
                });
            }
        }
        return ResponseEntity.ok().build();
    }

    public static class CacheUpdateRequest {
        private ArrayList<String> folderUris;

        public Optional<ArrayList<String>> getFolderUris() {
            return Optional.of(folderUris);
        }

        CacheUpdateRequest() {}
    }
}
