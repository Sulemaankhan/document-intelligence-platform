package com.dip.service;

import tools.jackson.databind.json.JsonMapper;

final class EmbeddingJson {

    private EmbeddingJson() {
    }

    static String serialize(float[] embedding, JsonMapper mapper) {
        return mapper.writeValueAsString(embedding);
    }

    static float[] deserialize(String json, JsonMapper mapper) {
        return mapper.readValue(json, float[].class);
    }
}
