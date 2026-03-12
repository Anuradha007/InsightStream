package com.insightstream.embedding;

import java.util.List;

/**
 * Abstraction for generating embeddings from text.
 * Implementations may delegate to Ollama, OpenAI, or other providers.
 */
public interface EmbeddingProvider {

    /**
     * Embed a single text into a vector.
     */
    float[] embed(String text);

    /**
     * Embed multiple texts. Default implementation calls embed() in sequence.
     */
    default List<float[]> embed(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }
}
