package com.dominiccobo.fyp.langserver.sources;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "sources")
@Configuration
public class SourceConfiguration {

    private AggregateConfiguration documentation = new AggregateConfiguration(20, 70, 60);

    public final AggregateConfiguration getDocumentation() {
        return documentation;
    }

    public void setDocumentation(AggregateConfiguration documentation) {
        this.documentation = documentation;
    }

    public SourceConfiguration() {
    }

    public static class AggregateConfiguration {

        private Integer defaultAggregateBufferPageSize = 20;
        private Integer aggregateBufferMaximumItems = 70;
        private Integer defaultQueryTimeoutSeconds = 60;

        public AggregateConfiguration(Integer defaultAggregateBufferPageSize, Integer aggregateBufferMaximumItems,
                                      Integer defaultQueryTimeoutSeconds) {
            this.defaultAggregateBufferPageSize = defaultAggregateBufferPageSize;
            this.aggregateBufferMaximumItems = aggregateBufferMaximumItems;
            this.defaultQueryTimeoutSeconds = defaultQueryTimeoutSeconds;
        }

        public final Integer getDefaultAggregateBufferPageSize() {
            return defaultAggregateBufferPageSize;
        }

        public final Integer getAggregateBufferMaximumItems() {
            return aggregateBufferMaximumItems;
        }

        public void setDefaultAggregateBufferPageSize(Integer defaultAggregateBufferPageSize) {
            this.defaultAggregateBufferPageSize = defaultAggregateBufferPageSize;
        }

        public void setAggregateBufferMaximumItems(Integer aggregateBufferMaximumItems) {
            this.aggregateBufferMaximumItems = aggregateBufferMaximumItems;
        }

        public final Integer getDefaultQueryTimeoutSeconds() {
            return defaultQueryTimeoutSeconds;
        }

        public void setDefaultQueryTimeoutSeconds(Integer defaultQueryTimeoutSeconds) {
            this.defaultQueryTimeoutSeconds = defaultQueryTimeoutSeconds;
        }
    }
}

