package com.insightstream.document;

import com.insightstream.config.GlobalParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates parsing and chunking of uploaded documents.
 * Delegates to the appropriate parser and then to TextChunker.
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    public DocumentIngestionService(List<DocumentParser> parsers, TextChunker textChunker) {
        this.parsers = parsers;
        this.textChunker = textChunker;
    }
    private final List<DocumentParser> parsers;
    private final TextChunker textChunker;

    public List<DocumentChunk> ingest(InputStream inputStream, String filename, String contentType) throws Exception {
        DocumentParser parser = findParser(filename, contentType);
        if (parser == null) {
            throw new UnsupportedOperationException("No parser for file: " + filename + " (" + contentType + ")");
        }
        String fullText = parser.parse(inputStream, filename);
        if (fullText == null || fullText.isBlank()) {
            log.warn("Empty content for file: {}", filename);
            return List.of();
        }
        String documentId = UUID.randomUUID().toString();
        List<DocumentChunk> chunks = textChunker.chunk(documentId, filename, fullText);
        log.info("Ingested {} chunks from {} (docId={})", chunks.size(), filename, documentId);
        return chunks;
    }

    private DocumentParser findParser(String filename, String contentType) {
        for (DocumentParser p : parsers) {
            if (p.supports(filename, contentType)) {
                return p;
            }
        }
        return null;
    }
}
