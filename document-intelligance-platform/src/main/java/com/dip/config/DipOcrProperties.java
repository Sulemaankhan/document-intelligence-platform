package com.dip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dip.ocr")
public final class DipOcrProperties {

    private final boolean enabled;
    private final int dpi;
    private final int maxPages;
    private final String language;
    private final String dataPath;

    public DipOcrProperties(boolean enabled, int dpi, int maxPages, String language, String dataPath) {
        this.enabled = enabled;
        this.dpi = dpi;
        this.maxPages = maxPages;
        this.language = language;
        this.dataPath = dataPath;
    }

    public boolean enabled() {
        return enabled;
    }

    public int dpi() {
        return dpi;
    }

    public int maxPages() {
        return maxPages;
    }

    public String language() {
        return language;
    }

    public String dataPath() {
        return dataPath;
    }
}
