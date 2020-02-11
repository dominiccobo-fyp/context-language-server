package com.dominiccobo.fyp.langserver.sources.documentation;

import com.dominiccobo.fyp.context.models.Documentation;
import com.dominiccobo.fyp.context.models.DocumentationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class DocumentationController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentationController.class);
    private final DocumentationAggregateService aggregateService;

    @Autowired
    public DocumentationController(DocumentationAggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @GetMapping("/documentation/{queryIdentifier}")
    public List<Documentation> get(@PathVariable(required = true) String queryIdentifier,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int limit) {

        return aggregateService.queryForItems(queryIdentifier).stream()
                .skip(page * limit)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @GetMapping("/documentation")
    public Response getQueryFor(@RequestParam String uri, @RequestParam String query) throws IOException, URISyntaxException {
        LOG.info("Received query creation request for ...");

        DocumentationAggregate.DocumentationRequestContext ctx = new DocumentationAggregate.DocumentationRequestContext(uri, query, DocumentationType.QA);

        DocumentationAggregate aggregate = aggregateService.createAggregate(ctx);
        aggregateService.run(aggregate);
        return new Response(aggregate.getIdentifier(), "Aggregate created. Query /documentation/{id} for results.");
    }

    public static class Response {
        private String identifier;
        private String message;

        Response() {}

        public Response(String identifier, String message) {
            this.identifier = identifier;
            this.message = message;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getMessage() {
            return message;
        }
    }
}
