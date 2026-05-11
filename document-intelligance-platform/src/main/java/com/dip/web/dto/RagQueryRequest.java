package com.dip.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Question answered from retrieved document passages (RAG)")
public class RagQueryRequest {

    @NotBlank
    @Schema(description = "User question", example = "What are the payment terms?")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
