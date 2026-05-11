package com.dip.service;

public final class PdfExtractionResult {

    private final String text;
    private final PdfExtractionSource source;

    public PdfExtractionResult(String text, PdfExtractionSource source) {
        this.text = text;
        this.source = source;
    }

    public String text() {
        return text;
    }

    public PdfExtractionSource source() {
        return source;
    }

    public boolean hasText() {
        return text != null && !text.isBlank();
    }
}
