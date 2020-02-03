package com.dominiccobo.fyp.langserver.sources.workitems;

import com.dominiccobo.fyp.context.models.WorkItem;
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
public class WorkItemsController {

    private static final Logger LOG = LoggerFactory.getLogger(WorkItemsController.class);
    private final WorkItemAggregateService workItemAggregateService;

    @Autowired
    public WorkItemsController(WorkItemAggregateService workItemAggregateService) {
        this.workItemAggregateService = workItemAggregateService;
    }

    @GetMapping("/workItems/{queryIdentifier}")
    public List<WorkItem> get(@PathVariable(required = true) String queryIdentifier,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int limit) throws IOException, URISyntaxException {

        return workItemAggregateService.queryForWorkItems(queryIdentifier).stream()
                .skip(page * limit)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @GetMapping("/workItems")
    public Response getQueryFor(@RequestParam String uri) throws IOException, URISyntaxException {
        LOG.info("Received query creation request for ...");
        WorkItemsAggregate aggregate = workItemAggregateService.createWorkItemsAggregate(uri);
        workItemAggregateService.run(aggregate);
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
