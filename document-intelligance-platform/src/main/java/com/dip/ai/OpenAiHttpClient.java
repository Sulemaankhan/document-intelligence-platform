package com.dip.ai;

import com.dip.config.DipAiProperties;
import com.dip.rag.RetrievedChunk;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiHttpClient {

    private final DipAiProperties props;
    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    public OpenAiHttpClient(DipAiProperties props, RestClient openAiRestClient, JsonMapper jsonMapper) {
        this.props = props;
        this.restClient = openAiRestClient;
        this.jsonMapper = jsonMapper;
    }

    public boolean isAvailable() {
        return props.enabled()
                && props.apiKey() != null
                && !props.apiKey().isBlank();
    }

    public String summarizeDocument(String extractedText) throws IOException {
        String body = extractedText == null ? "" : extractedText;
        int maxChars = 120_000;
        if (body.length() > maxChars) {
            body = body.substring(0, maxChars) + "\n\n[... truncated for summarization ...]";
        }
        String system = "You summarize uploaded documents for business users. Respond with a concise summary "
                + "(bullet points allowed). Capture purpose, key facts, dates, parties, and action items "
                + "when present. If the text is empty or unreadable, say so briefly.";
        return chatCompletion(system, body, 0.3);
    }

    /**
     * RAG generation: answer using only the provided retrieved passages.
     */
    public String ragAnswer(String question, List<RetrievedChunk> chunks, int maxContextChars, double temperature) throws IOException {
        StringBuilder context = new StringBuilder();
        int used = 0;
        int sourceNum = 1;
        for (RetrievedChunk c : chunks) {
            String header = "[Source %d | document=%s | chunk=%d | relevance=%.4f]\n"
                    .formatted(sourceNum++, c.filename(), c.chunkIndex(), c.score());
            String body = c.text() == null ? "" : c.text();
            String block = header + body + "\n\n";
            if (used + block.length() > maxContextChars) {
                int room = maxContextChars - used - header.length();
                if (room < 80) {
                    break;
                }
                body = body.substring(0, Math.min(body.length(), room));
                block = header + body + "\n\n";
            }
            context.append(block);
            used += block.length();
            if (used >= maxContextChars) {
                break;
            }
        }
        String system = "You are a retrieval-augmented assistant. Answer the user's question using ONLY the CONTEXT "
                + "passages below. If the context does not contain enough information, say clearly that the "
                + "uploaded documents do not specify it. Cite supporting facts with the document filename (and "
                + "chunk index when helpful). Do not invent facts outside the context.";
        String user = "QUESTION:\n" + question + "\n\nCONTEXT:\n" + context;
        return chatCompletion(system, user, temperature);
    }

    public List<float[]> embedTexts(List<String> inputs) throws IOException {
        if (inputs.isEmpty()) {
            return List.of();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.embeddingModel());
        body.put("input", inputs);

        String raw = restClient.post()
                .uri("/v1/embeddings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode root = jsonMapper.readTree(raw);
        JsonNode data = root.path("data");
        List<float[]> out = new ArrayList<>();
        for (JsonNode item : data) {
            JsonNode emb = item.path("embedding");
            if (!emb.isArray()) {
                continue;
            }
            float[] vec = new float[emb.size()];
            int i = 0;
            for (JsonNode n : emb) {
                vec[i++] = (float) n.asDouble();
            }
            out.add(vec);
        }
        if (out.size() != inputs.size()) {
            throw new IOException("Embedding response size mismatch: expected " + inputs.size() + ", got " + out.size());
        }
        return out;
    }

    private String chatCompletion(String systemPrompt, String userMessage, double temperature) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.chatModel());
        body.put("temperature", temperature);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        String raw = restClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode root = jsonMapper.readTree(raw);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        return content.asText("").trim();
    }
}
