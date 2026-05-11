package com.dip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dip.chunk")
public final class DipChunkProperties {

    private final int size;
    private final int overlap;

    public DipChunkProperties(int size, int overlap) {
        this.size = size;
        this.overlap = overlap;
    }

    public int size() {
        return size;
    }

    public int overlap() {
        return overlap;
    }
}
