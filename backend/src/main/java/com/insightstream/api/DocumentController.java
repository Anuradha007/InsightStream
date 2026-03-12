package com.insightstream.api;

import com.insightstream.document.DocumentIngestionService;
import com.insightstream.document.DocumentChunk;
import com.insightstream.vector.HybridSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.insightstream.document.SeedDataService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    public DocumentController(DocumentIngestionService ingestionService, HybridSearchService hybridSearchService, SeedDataService seedDataService) {
        this.ingestionService = ingestionService;
        this.hybridSearchService = hybridSearchService;
        this.seedDataService = seedDataService;
    }
    private final DocumentIngestionService ingestionService;
    private final HybridSearchService hybridSearchService;
    private final SeedDataService seedDataService;

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> ingest(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }
        try {
            List<DocumentChunk> chunks = ingestionService.ingest(
                    file.getInputStream(),
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
                    file.getContentType()
            );
            hybridSearchService.addChunks(chunks);
            return ResponseEntity.ok(Map.of(
                    "chunksIngested", chunks.size(),
                    "filename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
            ));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Ingest failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ingest open-source seed data from classpath seed-data/*.txt into the RAG store.
     * Call once to populate the database with bundled documents (e.g. public domain excerpts).
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed() {
        try {
            List<Map<String, Object>> results = seedDataService.seed();
            int totalChunks = results.stream()
                    .filter(r -> r.containsKey("chunksIngested"))
                    .mapToInt(r -> (Integer) r.get("chunksIngested"))
                    .sum();
            return ResponseEntity.ok(Map.of(
                    "filesIngested", results.size(),
                    "totalChunks", totalChunks,
                    "details", results
            ));
        } catch (Exception e) {
            log.error("Seed failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
