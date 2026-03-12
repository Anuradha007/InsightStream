package com.insightstream.document;

import java.io.InputStream;

/**
 * Abstraction for parsing a document (e.g. PDF, plain text) into raw text.
 * Implementations are format-specific.
 */
public interface DocumentParser {

    /**
     * Whether this parser supports the given filename/content type.
     */
    boolean supports(String filename, String contentType);

    /**
     * Parse the stream into plain text.
     */
    String parse(InputStream inputStream, String filename) throws Exception;
}
