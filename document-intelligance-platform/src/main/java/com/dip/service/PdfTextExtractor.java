package com.dip.service;

import com.dip.config.DipOcrProperties;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Extracts text from PDFs: native text layer first, then optional OCR for scanned documents.
 */
@Service
public class PdfTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(PdfTextExtractor.class);

    private final DipOcrProperties ocrProperties;

    public PdfTextExtractor(DipOcrProperties ocrProperties) {
        this.ocrProperties = ocrProperties;
    }

    public PdfExtractionResult extract(Path pdfPath) {
        try (PDDocument doc = loadPdf(pdfPath)) {
            if (doc.getNumberOfPages() == 0) {
                return new PdfExtractionResult("", PdfExtractionSource.EMPTY);
            }

            var access = doc.getCurrentAccessPermission();
            if (access != null && !access.canExtractContent()) {
                log.warn("PDF {} disallows text extraction (permissions)", pdfPath.getFileName());
            }

            String nativeText = extractNativePerPage(doc);
            if (nativeText != null && !nativeText.isBlank()) {
                return new PdfExtractionResult(nativeText.trim(), PdfExtractionSource.NATIVE);
            }

            log.debug("No native text layer (or empty) for {}; scanned PDF likely", pdfPath.getFileName());

            if (!ocrProperties.enabled()) {
                log.info("OCR is disabled (dip.ocr.enabled=false). Enable it and install Tesseract for scanned PDFs.");
                return new PdfExtractionResult("", PdfExtractionSource.EMPTY);
            }

            String ocrText = extractOcr(doc);
            if (ocrText != null && !ocrText.isBlank()) {
                return new PdfExtractionResult(ocrText.trim(), PdfExtractionSource.OCR);
            }

            return new PdfExtractionResult("", PdfExtractionSource.EMPTY);
        } catch (IOException e) {
            log.error("Failed to read PDF {}", pdfPath, e);
            return new PdfExtractionResult("", PdfExtractionSource.EMPTY);
        }
    }

    private static PDDocument loadPdf(Path pdfPath) throws IOException {
        try {
            return Loader.loadPDF(pdfPath.toFile());
        } catch (IOException first) {
            try {
                return Loader.loadPDF(pdfPath.toFile(), "");
            } catch (IOException second) {
                log.debug("Retry with empty password failed: {}", second.getMessage());
                throw first;
            }
        }
    }

    private static String extractNativePerPage(PDDocument doc) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);

        int pages = doc.getNumberOfPages();
        StringBuilder sb = new StringBuilder();
        for (int p = 1; p <= pages; p++) {
            stripper.setStartPage(p);
            stripper.setEndPage(p);
            try {
                sb.append(stripper.getText(doc));
                sb.append('\n');
            } catch (Exception e) {
                log.warn("Native text extraction failed for page {}/{}: {}", p, pages, e.getMessage());
            }
        }
        return sb.toString();
    }

    private String extractOcr(PDDocument doc) {
        int dpi = Math.max(72, Math.min(ocrProperties.dpi(), 600));
        int maxPages = Math.max(1, ocrProperties.maxPages());
        Tesseract tesseract = new Tesseract();
        if (ocrProperties.dataPath() != null && !ocrProperties.dataPath().isBlank()) {
            tesseract.setDatapath(ocrProperties.dataPath().trim());
        }
        tesseract.setLanguage(ocrProperties.language() != null ? ocrProperties.language() : "eng");

        PDFRenderer renderer = new PDFRenderer(doc);
        StringBuilder sb = new StringBuilder();
        int total = doc.getNumberOfPages();
        int limit = Math.min(total, maxPages);
        if (total > maxPages) {
            log.warn("OCR limited to first {} of {} pages (dip.ocr.max-pages)", maxPages, total);
        }
        for (int i = 0; i < limit; i++) {
            try {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                String pageText = tesseract.doOCR(image);
                sb.append(pageText).append('\n');
            } catch (TesseractException e) {
                log.error("OCR failed on page {} (install Tesseract OCR and eng traineddata): {}", i + 1, e.getMessage());
            } catch (IOException e) {
                log.warn("Failed to render page {} for OCR: {}", i + 1, e.getMessage());
            }
        }
        return sb.toString();
    }
}
