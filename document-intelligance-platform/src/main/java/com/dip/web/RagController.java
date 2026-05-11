package com.dip.web;

import com.dip.rag.RagQueryService;
import com.dip.web.dto.RagQueryRequest;
import com.dip.web.dto.RagQueryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/rag")
@Tag(name = "RAG", description = "Ask questions; answers use retrieved PDF passages only")
public class RagController {

    private final RagQueryService ragQueryService;

    public RagController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @Operation(summary = "Ask a question (RAG)",
            description = "Retrieves relevant passages (embeddings first, then keyword fallback), then generates an answer grounded in that context.")
    @PostMapping("/query")
    public RagQueryResponse query(@Valid @RequestBody RagQueryRequest request) throws IOException {
        return ragQueryService.answer(request.getQuestion());
    }
}
