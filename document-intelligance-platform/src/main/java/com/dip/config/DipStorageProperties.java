package com.dip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dip.storage")
public final class DipStorageProperties {

    private final String root;

    public DipStorageProperties(String root) {
        this.root = root;
    }

    public String root() {
        return root;
    }
}
