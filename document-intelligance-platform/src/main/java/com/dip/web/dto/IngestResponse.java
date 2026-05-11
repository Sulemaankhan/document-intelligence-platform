package com.dip.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of ingesting a PDF into the RAG knowledge base")
public final class IngestResponse {

    private final UUID id;
    private final String originalFilename;
    private final Instant createdAt;
    private final boolean chunksIndexed;
    private final String extractionSource;
    private final String summary;

    public IngestResponse(
            UUID id,
            String originalFilename,
            Instant createdAt,
            boolean chunksIndexed,
            String extractionSource,
            String summary
    ) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.createdAt = createdAt;
        this.chunksIndexed = chunksIndexed;
        this.extractionSource = extractionSource;
        this.summary = summary;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isChunksIndexed() {
        return chunksIndexed;
    }

    public String getExtractionSource() {
        return extractionSource;
    }

    public String getSummary() {
        return summary;
    }
}
