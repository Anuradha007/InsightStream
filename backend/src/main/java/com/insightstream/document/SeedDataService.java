package com.insightstream.document;

import com.insightstream.vector.HybridSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads and ingests open-source seed data from classpath seed-data/ into the RAG store.
 */
@Service
public class SeedDataService {

    private static final Logger log = LoggerFactory.getLogger(SeedDataService.class);
    private static final String SEED_LOCATION = "classpath:seed-data/*.txt";

    private final DocumentIngestionService ingestionService;
    private final HybridSearchService hybridSearchService;

    public SeedDataService(DocumentIngestionService ingestionService, HybridSearchService hybridSearchService) {
        this.ingestionService = ingestionService;
        this.hybridSearchService = hybridSearchService;
    }

    /**
     * Ingest all .txt seed files from classpath seed-data/.
     * Returns summary of ingested files and total chunks.
     */
    public List<Map<String, Object>> seed() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(SEED_LOCATION);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) continue;
            try (InputStream is = resource.getInputStream()) {
                List<DocumentChunk> chunks = ingestionService.ingest(is, filename, "text/plain");
                hybridSearchService.addChunks(chunks);
                results.add(Map.of(
                        "filename", filename,
                        "chunksIngested", chunks.size()
                ));
                log.info("Seed ingested: {} ({} chunks)", filename, chunks.size());
            } catch (Exception e) {
                log.warn("Seed failed for {}: {}", filename, e.getMessage());
                results.add(Map.of(
                        "filename", filename,
                        "error", e.getMessage()
                ));
            }
        }
        return results;
    }
}
