package com.dip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dip.ai")
public final class DipAiProperties {

    private final boolean enabled;
    private final String baseUrl;
    private final String apiKey;
    private final String chatModel;
    private final String embeddingModel;

    public DipAiProperties(boolean enabled, String baseUrl, String apiKey, String chatModel, String embeddingModel) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    public boolean enabled() {
        return enabled;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String apiKey() {
        return apiKey;
    }

    public String chatModel() {
        return chatModel;
    }

    public String embeddingModel() {
        return embeddingModel;
    }
}
