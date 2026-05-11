package com.dip.web;

import com.dip.service.DocumentApplicationService;
import com.dip.web.dto.IngestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Ingest", description = "Add PDFs to the RAG knowledge base (extract text, chunk, embed)")
public class DocumentController {

    private final DocumentApplicationService documentApplicationService;

    public DocumentController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @Operation(summary = "Ingest a PDF",
            description = "Extracts text (native + optional OCR), summarizes when OPENAI_API_KEY is set, chunks/embeds for RAG.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public IngestResponse ingest(
            @Parameter(description = "PDF document", required = true)
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        return documentApplicationService.ingestPdf(file);
    }

    @Operation(summary = "Reindex a document", description = "Rebuilds chunks and embeddings for an existing document id.")
    @PostMapping("/{id}/reindex")
    public IngestResponse reindex(@PathVariable UUID id) {
        return documentApplicationService.reindex(id);
    }
}
