package com.insightstream.document;

/**
 * Represents a single chunk of document content for indexing and retrieval.
 */
public class DocumentChunk {

    private String id;
    private String documentId;
    private String sourceName;
    private int index;
    private String text;
    private String metadata;

    public DocumentChunk() {}

    public DocumentChunk(String id, String documentId, String sourceName, int index, String text, String metadata) {
        this.id = id;
        this.documentId = documentId;
        this.sourceName = sourceName;
        this.index = index;
        this.text = text;
        this.metadata = metadata;
    }

    public static DocumentChunkBuilder builder() {
        return new DocumentChunkBuilder();
    }

    public static class DocumentChunkBuilder {
        private String id;
        private String documentId;
        private String sourceName;
        private int index;
        private String text;
        private String metadata;

        public DocumentChunkBuilder id(String id) { this.id = id; return this; }
        public DocumentChunkBuilder documentId(String documentId) { this.documentId = documentId; return this; }
        public DocumentChunkBuilder sourceName(String sourceName) { this.sourceName = sourceName; return this; }
        public DocumentChunkBuilder index(int index) { this.index = index; return this; }
        public DocumentChunkBuilder text(String text) { this.text = text; return this; }
        public DocumentChunkBuilder metadata(String metadata) { this.metadata = metadata; return this; }
        public DocumentChunk build() {
            return new DocumentChunk(id, documentId, sourceName, index, text, metadata);
        }
    }

    public String getId() { return id; }
    public String getDocumentId() { return documentId; }
    public String getSourceName() { return sourceName; }
    public int getIndex() { return index; }
    public String getText() { return text; }
    public String getMetadata() { return metadata; }
    public void setId(String id) { this.id = id; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public void setIndex(int index) { this.index = index; }
    public void setText(String text) { this.text = text; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
