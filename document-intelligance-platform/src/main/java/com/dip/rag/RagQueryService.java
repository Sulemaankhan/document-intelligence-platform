package com.dip.rag;

import com.dip.ai.OpenAiHttpClient;
import com.dip.config.DipRagProperties;
import com.dip.service.ChunkRetrievalService;
import com.dip.web.dto.RagQueryResponse;
import com.dip.web.dto.RagSourceDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RagQueryService {

    private static final int SOURCE_EXCERPT_MAX = 600;

    private final OpenAiHttpClient openAiHttpClient;
    private final ChunkRetrievalService chunkRetrievalService;
    private final DipRagProperties ragProperties;

    public RagQueryService(
            OpenAiHttpClient openAiHttpClient,
            ChunkRetrievalService chunkRetrievalService,
            DipRagProperties ragProperties
    ) {
        this.openAiHttpClient = openAiHttpClient;
        this.chunkRetrievalService = chunkRetrievalService;
        this.ragProperties = ragProperties;
    }

    public RagQueryResponse answer(String question) throws IOException {
        if (!openAiHttpClient.isAvailable()) {
            throw new RagUnavailableException(
                    "RAG requires an LLM: set OPENAI_API_KEY and ensure dip.ai.enabled=true.");
        }

        int k = Math.max(1, ragProperties.retrievalTopK());
        List<RetrievedChunk> chunks = chunkRetrievalService.retrieveSemantic(question, k);
        String retrievalMode = "semantic";

        if (chunks.isEmpty()) {
            chunks = chunkRetrievalService.retrieveKeyword(question, k);
            retrievalMode = "keyword";
        }

        if (chunks.isEmpty()) {
            return new RagQueryResponse(
                    "No relevant passages were found in the document store. Upload PDFs first or try different wording.",
                    "none",
                    List.of()
            );
        }

        String answer = openAiHttpClient.ragAnswer(
                question,
                chunks,
                ragProperties.maxContextChars(),
                ragProperties.answerTemperature()
        );

        List<RagSourceDto> sources = chunks.stream().map(RagQueryService::toSource).toList();
        return new RagQueryResponse(answer, retrievalMode, sources);
    }

    private static RagSourceDto toSource(RetrievedChunk c) {
        String excerpt = truncate(c.text(), SOURCE_EXCERPT_MAX);
        return new RagSourceDto(c.documentId(), c.filename(), c.chunkIndex(), c.score(), excerpt);
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "…";
    }
}
