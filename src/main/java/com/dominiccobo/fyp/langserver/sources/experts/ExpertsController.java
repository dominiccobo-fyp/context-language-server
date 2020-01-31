package com.dominiccobo.fyp.langserver.sources.experts;

import com.dominiccobo.fyp.context.models.Expert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@CrossOrigin
public class ExpertsController {

    private static final Logger LOG = LoggerFactory.getLogger(ExpertsController.class);
    private final ExpertsAggregateService aggregateService;

    @Autowired
    public ExpertsController(ExpertsAggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @GetMapping("/experts/{queryIdentifier}")
    public List<Expert> get(@PathVariable(required = true) String queryIdentifier,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int limit) throws IOException, URISyntaxException {

        return aggregateService.queryForExperts(queryIdentifier);
    }

    @GetMapping("/experts")
    public Response getQueryFor(@RequestParam String uri) throws IOException, URISyntaxException {
        LOG.info("Received query creation request for ...");
        ExpertsAggregate aggregate = aggregateService.createExpertsAggregate(uri);
        aggregateService.run(aggregate);
        return new Response(aggregate.getIdentifier(), "Aggregate created. Query /workItems/{id} for results.");
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
