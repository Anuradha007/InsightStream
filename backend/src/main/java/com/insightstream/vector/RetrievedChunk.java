package com.insightstream.vector;

/**
 * A single chunk retrieved from hybrid search, with optional RRF score.
 */
public class RetrievedChunk {

    private String chunkId;
    private String text;
    private String sourceName;
    private String documentId;
    private double rrfScore;

    public RetrievedChunk() {}

    public RetrievedChunk(String chunkId, String text, String sourceName, String documentId, double rrfScore) {
        this.chunkId = chunkId;
        this.text = text;
        this.sourceName = sourceName;
        this.documentId = documentId;
        this.rrfScore = rrfScore;
    }

    public static RetrievedChunkBuilder builder() {
        return new RetrievedChunkBuilder();
    }

    public static class RetrievedChunkBuilder {
        private String chunkId;
        private String text;
        private String sourceName;
        private String documentId;
        private double rrfScore;

        public RetrievedChunkBuilder chunkId(String chunkId) { this.chunkId = chunkId; return this; }
        public RetrievedChunkBuilder text(String text) { this.text = text; return this; }
        public RetrievedChunkBuilder sourceName(String sourceName) { this.sourceName = sourceName; return this; }
        public RetrievedChunkBuilder documentId(String documentId) { this.documentId = documentId; return this; }
        public RetrievedChunkBuilder rrfScore(double rrfScore) { this.rrfScore = rrfScore; return this; }
        public RetrievedChunk build() {
            return new RetrievedChunk(chunkId, text, sourceName, documentId, rrfScore);
        }
    }

    public String getChunkId() { return chunkId; }
    public String getText() { return text; }
    public String getSourceName() { return sourceName; }
    public String getDocumentId() { return documentId; }
    public double getRrfScore() { return rrfScore; }
}
