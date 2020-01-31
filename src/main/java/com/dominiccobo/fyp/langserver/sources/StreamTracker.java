package com.dominiccobo.fyp.langserver.sources;

import com.dominiccobo.fyp.context.models.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StreamTracker<StreamType> {

    private static final Logger LOG = LoggerFactory.getLogger(StreamTracker.class);

    // TODO: a queue is probably more suited, but do we reaally know how we are
    // TODO: this further raises questions as to the appropriateness of Axon as the messaging solution... perhaps Rabbit is better?
    private final List<StreamType> buffer = new ArrayList<>();

    public StreamTracker(Stream<StreamType> results) {
        results.forEach(e -> {
            buffer.add(e);
            LOG.debug("Adding {}", ((WorkItem) e).getTitle());
        });
    }

    public List<StreamType> getItems() {
        return buffer;
    }
}
