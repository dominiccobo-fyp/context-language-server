package com.dominiccobo.fyp.langserver;

import com.dominiccobo.fyp.context.models.WorkItem;
import com.dominiccobo.fyp.langserver.aggregation.AggregateService;
import com.dominiccobo.fyp.langserver.aggregation.WorkItemsAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@CrossOrigin
public class WorkItemsController {

    private static final Logger LOG = LoggerFactory.getLogger(WorkItemsController.class);
    private final AggregateService aggregateService;

    @Autowired
    public WorkItemsController(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @GetMapping("/workItems/{queryIdentifier}")
    public List<WorkItem> get(@PathVariable(required = true) String queryIdentifier,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int limit) throws IOException, URISyntaxException {

        return aggregateService.queryForWorkItems(queryIdentifier);
    }

    @GetMapping("/workItems")
    public String getQueryFor(@RequestParam String uri) throws IOException, URISyntaxException {
        LOG.info("Received query creation request for ...");
        WorkItemsAggregate aggregate = aggregateService.createWorkItemsAggregate(uri);
        aggregateService.run(aggregate);
        return aggregate.getIdentifier();
    }
}
