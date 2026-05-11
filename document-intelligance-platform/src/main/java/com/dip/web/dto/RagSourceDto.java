package com.dip.web.dto;

import java.util.UUID;

public final class RagSourceDto {

    private final UUID documentId;
    private final String filename;
    private final int chunkIndex;
    private final double relevanceScore;
    private final String excerpt;

    public RagSourceDto(UUID documentId, String filename, int chunkIndex, double relevanceScore, String excerpt) {
        this.documentId = documentId;
        this.filename = filename;
        this.chunkIndex = chunkIndex;
        this.relevanceScore = relevanceScore;
        this.excerpt = excerpt;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getFilename() {
        return filename;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public String getExcerpt() {
        return excerpt;
    }
}
