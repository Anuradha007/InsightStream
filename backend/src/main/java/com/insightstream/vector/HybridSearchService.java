package com.insightstream.vector;

import com.insightstream.config.HybridSearchProperties;
import com.insightstream.document.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid retrieval: BM25 + semantic search with Reciprocal Rank Fusion (RRF).
 * RRF score = sum over rankings of 1 / (k + rank); default k=60.
 */
@Service
public class HybridSearchService {

    private static final Logger log = LoggerFactory.getLogger(HybridSearchService.class);

    public HybridSearchService(SemanticSearchService semanticSearchService, Bm25Index bm25Index, HybridSearchProperties hybridSearchProperties) {
        this.semanticSearchService = semanticSearchService;
        this.bm25Index = bm25Index;
        this.hybridSearchProperties = hybridSearchProperties;
    }
    private final SemanticSearchService semanticSearchService;
    private final Bm25Index bm25Index;
    private final HybridSearchProperties hybridSearchProperties;

    /**
     * Add chunks to both Chroma (semantic) and BM25 index.
     */
    public void addChunks(List<DocumentChunk> chunks) throws Exception {
        semanticSearchService.addChunks(chunks);
        bm25Index.addChunks(chunks);
    }

    /**
     * Hybrid search: run semantic and BM25, merge with RRF, return deduplicated chunks.
     */
    public List<RetrievedChunk> search(String query, int topK) {
        int semanticK = hybridSearchProperties.getSemanticTopK();
        int bm25K = hybridSearchProperties.getBm25TopK();
        int rrfK = hybridSearchProperties.getRrfK();

        List<ChromaApiClient.ChromaQueryResult> semanticResults = semanticSearchService.search(query, semanticK);
        List<Bm25Index.Bm25Result> bm25Results = bm25Index.search(query, bm25K);

        Map<String, Double> rrfScores = new HashMap<>();
        // RRF: 1 / (k + rank), rank 1-based
        for (int i = 0; i < semanticResults.size(); i++) {
            String id = semanticResults.get(i).getId();
            rrfScores.merge(id, 1.0 / (rrfK + i + 1), Double::sum);
        }
        for (int i = 0; i < bm25Results.size(); i++) {
            String id = bm25Results.get(i).chunkId();
            rrfScores.merge(id, 1.0 / (rrfK + i + 1), Double::sum);
        }

        // Build chunk id -> text and metadata from both result sets
        Map<String, String> idToText = new HashMap<>();
        Map<String, String> idToSource = new HashMap<>();
        Map<String, String> idToDocId = new HashMap<>();
        for (ChromaApiClient.ChromaQueryResult r : semanticResults) {
            idToText.put(r.getId(), r.getDocument());
            if (r.getMetadata() != null) {
                idToSource.put(r.getId(), (String) r.getMetadata().getOrDefault("sourceName", ""));
                idToDocId.put(r.getId(), (String) r.getMetadata().getOrDefault("documentId", ""));
            }
        }
        for (Bm25Index.Bm25Result r : bm25Results) {
            idToText.putIfAbsent(r.chunkId(), r.text());
        }

        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(e -> RetrievedChunk.builder()
                        .chunkId(e.getKey())
                        .text(idToText.getOrDefault(e.getKey(), ""))
                        .sourceName(idToSource.getOrDefault(e.getKey(), ""))
                        .documentId(idToDocId.getOrDefault(e.getKey(), ""))
                        .rrfScore(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
