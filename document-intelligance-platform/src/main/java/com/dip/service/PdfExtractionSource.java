package com.dip.service;

public enum PdfExtractionSource {
    /** Text layer read by PDFBox */
    NATIVE,
    /** Rasterized pages + Tesseract */
    OCR,
    /** No recoverable text */
    EMPTY
}
