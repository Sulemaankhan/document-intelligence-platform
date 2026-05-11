package com.dip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID documentId;

    private int chunkIndex;

    @Column(columnDefinition = "LONGTEXT")
    private String text;

    @Column(columnDefinition = "LONGTEXT")
    private String embeddingJson;

    protected DocumentChunk() {
    }

    public DocumentChunk(UUID documentId, int chunkIndex, String text, String embeddingJson) {
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.text = text;
        this.embeddingJson = embeddingJson;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getText() {
        return text;
    }

    public String getEmbeddingJson() {
        return embeddingJson;
    }
}
