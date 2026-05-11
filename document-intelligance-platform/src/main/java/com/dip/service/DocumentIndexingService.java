package com.dip.service;

import com.dip.ai.OpenAiHttpClient;
import com.dip.config.DipChunkProperties;
import com.dip.domain.Document;
import com.dip.domain.DocumentChunk;
import com.dip.repo.DocumentChunkRepository;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentIndexingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndexingService.class);
    private static final int EMBED_BATCH = 16;

    private final DocumentChunkRepository chunkRepository;
    private final TextChunker textChunker;
    private final DipChunkProperties chunkProperties;
    private final OpenAiHttpClient openAi;
    private final JsonMapper jsonMapper;

    public DocumentIndexingService(
            DocumentChunkRepository chunkRepository,
            TextChunker textChunker,
            DipChunkProperties chunkProperties,
            OpenAiHttpClient openAi,
            JsonMapper jsonMapper
    ) {
        this.chunkRepository = chunkRepository;
        this.textChunker = textChunker;
        this.chunkProperties = chunkProperties;
        this.openAi = openAi;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void indexDocument(Document document) {
        UUID docId = document.getId();
        chunkRepository.deleteByDocumentId(docId);

        if (!openAi.isAvailable()) {
            log.info("Skipping embedding index for document {} (OPENAI_API_KEY not configured)", docId);
            return;
        }

        String text = document.getExtractedText();
        List<String> parts = textChunker.chunk(text, chunkProperties.size(), chunkProperties.overlap());
        if (parts.isEmpty()) {
            return;
        }

        try {
            int index = 0;
            for (int i = 0; i < parts.size(); i += EMBED_BATCH) {
                List<String> batch = parts.subList(i, Math.min(i + EMBED_BATCH, parts.size()));
                List<float[]> vectors = openAi.embedTexts(batch);
                List<DocumentChunk> rows = new ArrayList<>();
                for (int j = 0; j < batch.size(); j++) {
                    String json = EmbeddingJson.serialize(vectors.get(j), jsonMapper);
                    rows.add(new DocumentChunk(docId, index++, batch.get(j), json));
                }
                chunkRepository.saveAll(rows);
            }
        } catch (IOException e) {
            log.error("Failed to index document {}", docId, e);
            throw new IllegalStateException("Embedding index failed: " + e.getMessage(), e);
        }
    }
}
