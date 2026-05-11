package com.dip.rag;

import java.util.UUID;

/**
 * A single passage retrieved for RAG (embedding similarity or keyword match).
 */
public final class RetrievedChunk {

    private final UUID documentId;
    private final String filename;
    private final int chunkIndex;
    private final String text;
    private final double score;

    public RetrievedChunk(UUID documentId, String filename, int chunkIndex, String text, double score) {
        this.documentId = documentId;
        this.filename = filename;
        this.chunkIndex = chunkIndex;
        this.text = text;
        this.score = score;
    }

    public UUID documentId() {
        return documentId;
    }

    public String filename() {
        return filename;
    }

    public int chunkIndex() {
        return chunkIndex;
    }

    public String text() {
        return text;
    }

    public double score() {
        return score;
    }
}
