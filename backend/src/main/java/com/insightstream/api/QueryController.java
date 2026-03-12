package com.insightstream.api;

import com.insightstream.rag.RagQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    public QueryController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }
    private final RagQueryService ragQueryService;

    @PostMapping
    public ResponseEntity<RagQueryService.RagResponse> query(@RequestBody QueryRequest request) {
        int topK = request.getTopK() != null && request.getTopK() > 0 ? request.getTopK() : 10;
        RagQueryService.RagResponse response = ragQueryService.query(request.getQuery(), topK);
        return ResponseEntity.ok(response);
    }

    public static class QueryRequest {
        private String query;
        private Integer topK = 10;
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }
    }
}
