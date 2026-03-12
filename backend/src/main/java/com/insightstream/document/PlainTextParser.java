package com.insightstream.document;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class PlainTextParser implements DocumentParser {

    @Override
    public boolean supports(String filename, String contentType) {
        if (filename != null && filename.toLowerCase().endsWith(".txt")) return true;
        return contentType != null && (
                contentType.contains("text/plain") ||
                contentType.contains("text/markdown")
        );
    }

    @Override
    public String parse(InputStream inputStream, String filename) throws Exception {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
