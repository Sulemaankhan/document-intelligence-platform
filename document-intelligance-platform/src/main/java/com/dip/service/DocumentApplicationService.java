package com.dip.service;

import com.dip.ai.OpenAiHttpClient;
import com.dip.domain.Document;
import com.dip.repo.DocumentChunkRepository;
import com.dip.repo.DocumentRepository;
import com.dip.web.dto.IngestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Service
public class DocumentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DocumentApplicationService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final FileStorageService fileStorageService;
    private final PdfTextExtractor pdfTextExtractor;
    private final OpenAiHttpClient openAiHttpClient;
    private final DocumentIndexingService documentIndexingService;

    public DocumentApplicationService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            FileStorageService fileStorageService,
            PdfTextExtractor pdfTextExtractor,
            OpenAiHttpClient openAiHttpClient,
            DocumentIndexingService documentIndexingService
    ) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.fileStorageService = fileStorageService;
        this.pdfTextExtractor = pdfTextExtractor;
        this.openAiHttpClient = openAiHttpClient;
        this.documentIndexingService = documentIndexingService;
    }

    public IngestResponse ingestPdf(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.pdf";
        if (!original.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        String ct = file.getContentType();
        if (ct != null && !ct.equals("application/pdf") && !ct.equals("application/x-pdf")) {
            log.debug("Accepting PDF with content type {}", ct);
        }

        UUID id = UUID.randomUUID();
        var path = fileStorageService.savePdf(id, file);
        PdfExtractionResult extraction = pdfTextExtractor.extract(path);
        String extracted = extraction.hasText() ? extraction.text() : "";

        Document doc = new Document(
                id,
                original,
                file.getContentType() != null ? file.getContentType() : "application/pdf",
                path.toString(),
                extracted,
                Instant.now()
        );
        doc = documentRepository.save(doc);

        if (openAiHttpClient.isAvailable() && !extracted.isBlank()) {
            try {
                doc.setSummary(openAiHttpClient.summarizeDocument(extracted));
                doc = documentRepository.save(doc);
            } catch (IOException e) {
                log.warn("Summarization failed for document {}", doc.getId(), e);
            }
        }

        try {
            documentIndexingService.indexDocument(doc);
        } catch (RuntimeException e) {
            log.error("Indexing failed for document {}", doc.getId(), e);
        }

        boolean indexed = chunkRepository.countByDocumentId(id) > 0;
        return toResponse(doc, indexed, extraction.source());
    }

    public IngestResponse reindex(UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));
        PdfExtractionResult extraction = pdfTextExtractor.extract(Path.of(doc.getStoragePath()));
        if (extraction.hasText()) {
            doc.setExtractedText(extraction.text());
            doc = documentRepository.save(doc);
        }

        if (openAiHttpClient.isAvailable() && doc.getExtractedText() != null && !doc.getExtractedText().isBlank()) {
            try {
                doc.setSummary(openAiHttpClient.summarizeDocument(doc.getExtractedText()));
                doc = documentRepository.save(doc);
            } catch (IOException e) {
                log.warn("Summarization failed on reindex for document {}", doc.getId(), e);
            }
        }

        documentIndexingService.indexDocument(doc);
        boolean indexed = chunkRepository.countByDocumentId(id) > 0;
        return toResponse(doc, indexed, extraction.source());
    }

    private static IngestResponse toResponse(Document doc, boolean chunksIndexed, PdfExtractionSource source) {
        return new IngestResponse(
                doc.getId(),
                doc.getOriginalFilename(),
                doc.getCreatedAt(),
                chunksIndexed,
                source.name(),
                doc.getSummary()
        );
    }

    public static final class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
