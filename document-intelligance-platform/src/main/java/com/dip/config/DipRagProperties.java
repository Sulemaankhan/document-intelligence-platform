package com.dip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dip.rag")
public final class DipRagProperties {

    private final int retrievalTopK;
    private final int maxContextChars;
    private final double answerTemperature;

    public DipRagProperties(int retrievalTopK, int maxContextChars, double answerTemperature) {
        this.retrievalTopK = retrievalTopK;
        this.maxContextChars = maxContextChars;
        this.answerTemperature = answerTemperature;
    }

    public int retrievalTopK() {
        return retrievalTopK;
    }

    public int maxContextChars() {
        return maxContextChars;
    }

    public double answerTemperature() {
        return answerTemperature;
    }
}
