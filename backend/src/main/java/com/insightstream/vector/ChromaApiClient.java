package com.insightstream.vector;

import com.insightstream.config.ChromaProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Low-level HTTP client for Chroma DB REST API (v2).
 * Handles collection creation (get_or_create), add, and query.
 */
@Component
public class ChromaApiClient {

    private static final Logger log = LoggerFactory.getLogger(ChromaApiClient.class);

    private final ChromaProperties chromaProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    /** v2 base path: /api/v2/tenants/{tenant}/databases/{database} */
    private final String v2BasePath;
    /** Cached collection UUID after get_or_create. */
    private volatile String collectionIdCache;

    public ChromaApiClient(ChromaProperties chromaProperties, ObjectMapper objectMapper) {
        this.chromaProperties = chromaProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(chromaProperties.getBaseUrl()).build();
        this.v2BasePath = "/api/v2/tenants/" + chromaProperties.getTenant() + "/databases/" + chromaProperties.getDatabase();
    }

    public void ensureCollection() {
        String name = chromaProperties.getCollectionName();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("get_or_create", true);
            String json = restClient.post()
                    .uri(v2BasePath + "/collections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            ChromaCollectionCreateResponse resp = objectMapper.readValue(json, ChromaCollectionCreateResponse.class);
            if (resp.id != null) {
                collectionIdCache = resp.id;
            }
        } catch (Exception e) {
            log.debug("Collection create/get: {}", e.getMessage());
        }
    }

    public void add(String collectionId, List<String> ids, List<float[]> embeddings, List<String> documents, List<Map<String, Object>> metadatas) {
        if (ids.isEmpty()) return;
        List<List<Double>> embeddingsDouble = new ArrayList<>();
        for (float[] e : embeddings) {
            List<Double> list = new ArrayList<>(e.length);
            for (float v : e) list.add((double) v);
            embeddingsDouble.add(list);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        body.put("embeddings", embeddingsDouble);
        body.put("documents", documents);
        body.put("metadatas", metadatas != null ? metadatas : Collections.emptyList());

        restClient.post()
                .uri(v2BasePath + "/collections/{id}/add", collectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ChromaQueryResult> query(String collectionId, float[] queryEmbedding, int topK) {
        List<List<Double>> queryEmbeddings = List.of(toDoubleList(queryEmbedding));
        Map<String, Object> body = new HashMap<>();
        body.put("query_embeddings", queryEmbeddings);
        body.put("n_results", topK);
        body.put("include", List.of("documents", "metadatas", "distances"));

        String json = restClient.post()
                .uri(v2BasePath + "/collections/{id}/query", collectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            ChromaQueryResponse resp = objectMapper.readValue(json, ChromaQueryResponse.class);
            if (resp.ids == null || resp.ids.isEmpty()) return List.of();
            List<String> ids = resp.ids.get(0);
            List<Double> distances = resp.distances != null && !resp.distances.isEmpty() ? resp.distances.get(0) : null;
            List<String> documents = resp.documents != null && !resp.documents.isEmpty() ? resp.documents.get(0) : null;
            List<Map<String, Object>> metadatas = resp.metadatas != null && !resp.metadatas.isEmpty() ? resp.metadatas.get(0) : null;

            List<ChromaQueryResult> results = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                double distance = distances != null && i < distances.size() ? distances.get(i).doubleValue() : 0.0;
                String doc = documents != null && i < documents.size() ? documents.get(i) : "";
                Map<String, Object> meta = metadatas != null && i < metadatas.size() ? metadatas.get(i) : Map.of();
                results.add(new ChromaQueryResult(ids.get(i), distance, doc, meta));
            }
            return results;
        } catch (Exception e) {
            log.warn("Chroma query parse error: {}", e.getMessage());
            return List.of();
        }
    }

    public String getOrCreateCollectionId() {
        if (collectionIdCache != null) {
            return collectionIdCache;
        }
        ensureCollection();
        if (collectionIdCache != null) {
            return collectionIdCache;
        }
        // Fallback: try listing collections (v2 returns a JSON array)
        try {
            String list = restClient.get()
                    .uri(v2BasePath + "/collections")
                    .retrieve()
                    .body(String.class);
            java.util.List<ChromaCollectionItem> items = objectMapper.readValue(list, new TypeReference<List<ChromaCollectionItem>>() {});
            String name = chromaProperties.getCollectionName();
            if (items != null) {
                for (ChromaCollectionItem c : items) {
                    if (name.equals(c.name)) {
                        collectionIdCache = c.id;
                        return c.id;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("List collections: {}", e.getMessage());
        }
        return chromaProperties.getCollectionName();
    }

    private static List<Double> toDoubleList(float[] a) {
        List<Double> list = new ArrayList<>(a.length);
        for (float v : a) list.add((double) v);
        return list;
    }

    public static class ChromaQueryResult {
        private final String id;
        private final double distance;
        private final String document;
        private final Map<String, Object> metadata;

        public ChromaQueryResult(String id, double distance, String document, Map<String, Object> metadata) {
            this.id = id;
            this.distance = distance;
            this.document = document;
            this.metadata = metadata;
        }
        public String getId() { return id; }
        public double getDistance() { return distance; }
        public String getDocument() { return document; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    private static class ChromaQueryResponse {
        @JsonProperty("ids") List<List<String>> ids;
        @JsonProperty("distances") List<List<Double>> distances;
        @JsonProperty("documents") List<List<String>> documents;
        @JsonProperty("metadatas") List<List<Map<String, Object>>> metadatas;
    }

    /** v2 create collection response (get_or_create). */
    private static class ChromaCollectionCreateResponse {
        @JsonProperty String id;
        @JsonProperty String name;
    }

    /** v2 list collections returns a JSON array of these. */
    private static class ChromaCollectionItem {
        @JsonProperty String id;
        @JsonProperty String name;
    }
}
