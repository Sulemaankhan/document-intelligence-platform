package com.dip.service;

import com.dip.ai.OpenAiHttpClient;
import com.dip.domain.Document;
import com.dip.domain.DocumentChunk;
import com.dip.rag.RetrievedChunk;
import com.dip.repo.DocumentChunkRepository;
import com.dip.repo.DocumentRepository;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Retrieves relevant text passages for RAG (embedding similarity, then keyword fallback).
 */
@Service
public class ChunkRetrievalService {

    private final DocumentChunkRepository chunkRepository;
    private final DocumentRepository documentRepository;
    private final OpenAiHttpClient openAi;
    private final JsonMapper jsonMapper;

    public ChunkRetrievalService(
            DocumentChunkRepository chunkRepository,
            DocumentRepository documentRepository,
            OpenAiHttpClient openAi,
            JsonMapper jsonMapper
    ) {
        this.chunkRepository = chunkRepository;
        this.documentRepository = documentRepository;
        this.openAi = openAi;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Top passages by embedding similarity over stored chunks.
     */
    public List<RetrievedChunk> retrieveSemantic(String query, int topK) throws IOException {
        if (!openAi.isAvailable()) {
            return List.of();
        }
        List<float[]> q = openAi.embedTexts(List.of(query));
        if (q.isEmpty()) {
            return List.of();
        }
        float[] queryVec = q.get(0);
        List<DocumentChunk> all = chunkRepository.findAll();
        List<Scored> scored = new ArrayList<>();
        for (DocumentChunk ch : all) {
            float[] vec = EmbeddingJson.deserialize(ch.getEmbeddingJson(), jsonMapper);
            double score = EmbeddingMath.cosineSimilarity(queryVec, vec);
            scored.add(new Scored(ch.getDocumentId(), ch.getChunkIndex(), ch.getText(), score));
        }
        int k = Math.max(1, topK);
        return scored.stream()
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(k)
                .map(s -> {
                    Document doc = documentRepository.findById(s.documentId()).orElse(null);
                    String filename = doc != null ? doc.getOriginalFilename() : "(unknown)";
                    return new RetrievedChunk(s.documentId(), filename, s.chunkIndex(), s.text(), s.score());
                })
                .toList();
    }

    /**
     * Substring match over full extracted document text when embeddings are unavailable or semantic retrieval is empty.
     */
    public List<RetrievedChunk> retrieveKeyword(String query, int topK) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            return List.of();
        }
        List<RetrievedChunk> hits = new ArrayList<>();
        List<Document> docs = documentRepository.findAll();
        for (Document d : docs) {
            String text = d.getExtractedText();
            if (text == null) {
                continue;
            }
            String lower = text.toLowerCase();
            if (lower.contains(q)) {
                int idx = lower.indexOf(q);
                int start = Math.max(0, idx - 80);
                int end = Math.min(text.length(), idx + q.length() + 120);
                String snippet = text.substring(start, end).trim();
                hits.add(new RetrievedChunk(d.getId(), d.getOriginalFilename(), 0, snippet, 1.0));
            }
        }
        int k = Math.max(1, topK);
        return hits.stream().limit(k).toList();
    }

    private static final class Scored {
        private final UUID documentId;
        private final int chunkIndex;
        private final String text;
        private final double score;

        private Scored(UUID documentId, int chunkIndex, String text, double score) {
            this.documentId = documentId;
            this.chunkIndex = chunkIndex;
            this.text = text;
            this.score = score;
        }

        private UUID documentId() {
            return documentId;
        }

        private int chunkIndex() {
            return chunkIndex;
        }

        private String text() {
            return text;
        }

        private double score() {
            return score;
        }
    }
}
