package com.dip.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "RAG answer grounded in retrieved chunks")
public final class RagQueryResponse {

    private final String answer;
    private final String retrievalMode;
    private final List<RagSourceDto> sources;

    public RagQueryResponse(String answer, String retrievalMode, List<RagSourceDto> sources) {
        this.answer = answer;
        this.retrievalMode = retrievalMode;
        this.sources = sources;
    }

    @Schema(description = "Generated answer using only retrieved context")
    public String getAnswer() {
        return answer;
    }

    @Schema(description = "How passages were retrieved")
    public String getRetrievalMode() {
        return retrievalMode;
    }

    @Schema(description = "Sources used for grounding")
    public List<RagSourceDto> getSources() {
        return sources;
    }
}
