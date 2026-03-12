package com.insightstream.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * EmbeddingProvider that delegates to Spring AI's EmbeddingModel.
 */
@Component
@Primary
public class SpringAiEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingModel embeddingModel;

    public SpringAiEmbeddingProvider(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        return embeddingModel.embed(texts);
    }
}
