package com.insightstream.document;

import com.insightstream.config.GlobalParameters;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits text into overlapping chunks using configurable size and overlap.
 * Modular, OOP design: chunking strategy is encapsulated.
 */
@Component
public class TextChunker {

    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("[.!?]\\s+");
    private static final Pattern PARAGRAPH_BOUNDARY = Pattern.compile("\\n\\s*\\n");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public TextChunker(GlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }
    private final GlobalParameters globalParameters;

    /**
     * Split text into chunks respecting size and overlap from config.
     */
    public List<DocumentChunk> chunk(String documentId, String sourceName, String fullText) {
        int chunkSize = globalParameters.getChunkSize();
        int overlap = globalParameters.getChunkOverlap();
        List<String> rawChunks = splitWithOverlap(fullText, chunkSize, overlap);

        List<DocumentChunk> chunks = new ArrayList<>(rawChunks.size());
        for (int i = 0; i < rawChunks.size(); i++) {
            chunks.add(DocumentChunk.builder()
                    .id(documentId + "_chunk_" + i)
                    .documentId(documentId)
                    .sourceName(sourceName)
                    .index(i)
                    .text(rawChunks.get(i))
                    .build());
        }
        return chunks;
    }

    /**
     * Prefer splitting on paragraph, then sentence, then fixed size with overlap.
     */
    private List<String> splitWithOverlap(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= chunkSize) {
            return List.of(normalized);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + chunkSize, normalized.length());
            if (end < normalized.length()) {
                // Try to break at paragraph or sentence boundary
                int breakPoint = findBreakPoint(normalized, start, end);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }
            chunks.add(normalized.substring(start, end).trim());
            start = end - overlap;
            if (start >= normalized.length()) break;
            if (start < 0) start = 0;
        }
        return chunks;
    }

    private int findBreakPoint(String text, int start, int end) {
        String segment = text.substring(start, end);
        int lastParagraph = segment.lastIndexOf("\n\n");
        if (lastParagraph > segment.length() / 2) {
            return start + lastParagraph + 2;
        }
        int lastSentence = -1;
        for (var m = SENTENCE_BOUNDARY.matcher(segment); m.find(); ) {
            lastSentence = m.end();
        }
        if (lastSentence > segment.length() / 2) {
            return start + lastSentence;
        }
        int lastSpace = segment.lastIndexOf(' ');
        if (lastSpace > segment.length() / 2) {
            return start + lastSpace + 1;
        }
        return end;
    }
}
