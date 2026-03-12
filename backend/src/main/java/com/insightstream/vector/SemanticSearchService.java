package com.insightstream.vector;

import com.insightstream.config.ChromaProperties;
import com.insightstream.embedding.EmbeddingProvider;
import com.insightstream.document.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Semantic search via Chroma DB and configurable embeddings.
 */
@Service
public class SemanticSearchService {

    public SemanticSearchService(ChromaApiClient chromaApiClient, ChromaProperties chromaProperties, EmbeddingProvider embeddingProvider) {
        this.chromaApiClient = chromaApiClient;
        this.chromaProperties = chromaProperties;
        this.embeddingProvider = embeddingProvider;
    }
    private final ChromaApiClient chromaApiClient;
    private final ChromaProperties chromaProperties;
    private final EmbeddingProvider embeddingProvider;

    private String collectionId;

    public void addChunks(List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) return;
        String collId = getOrCreateCollectionId();
        List<String> ids = chunks.stream().map(DocumentChunk::getId).toList();
        List<String> texts = chunks.stream().map(DocumentChunk::getText).toList();
        List<float[]> embeddings = embeddingProvider.embed(texts);
        List<Map<String, Object>> metadatas = chunks.stream()
                .map(c -> Map.<String, Object>of(
                        "documentId", c.getDocumentId(),
                        "sourceName", c.getSourceName(),
                        "index", c.getIndex()
                ))
                .collect(Collectors.toList());
        chromaApiClient.add(collId, ids, embeddings, texts, metadatas);
    }

    public List<ChromaApiClient.ChromaQueryResult> search(String query, int topK) {
        String collId = getOrCreateCollectionId();
        float[] queryEmbedding = embeddingProvider.embed(query);
        return chromaApiClient.query(collId, queryEmbedding, topK);
    }

    private String getOrCreateCollectionId() {
        if (collectionId == null) {
            collectionId = chromaApiClient.getOrCreateCollectionId();
        }
        return collectionId;
    }
}
