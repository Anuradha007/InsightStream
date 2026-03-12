package com.insightstream.rag;

import com.insightstream.agent.ChatAgentRunner;
import com.insightstream.config.HybridSearchProperties;
import com.insightstream.vector.HybridSearchService;
import com.insightstream.vector.RetrievedChunk;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG orchestration: hybrid retrieval + config-driven agent to generate answer.
 */
@Service
public class RagQueryService {

    private static final String TEAM = "QueryTeam";
    private static final String AGENT = "query_agent";
    private static final String TASK = "answer";

    public RagQueryService(HybridSearchService hybridSearchService, ChatAgentRunner chatAgentRunner, HybridSearchProperties hybridSearchProperties) {
        this.hybridSearchService = hybridSearchService;
        this.chatAgentRunner = chatAgentRunner;
        this.hybridSearchProperties = hybridSearchProperties;
    }

    private final HybridSearchService hybridSearchService;
    private final ChatAgentRunner chatAgentRunner;
    private final HybridSearchProperties hybridSearchProperties;

    /**
     * Run hybrid search and generate an answer using the configured query agent.
     */
    public RagResponse query(String userQuery, int topK) {
        int k = topK > 0 ? topK : 10;
        List<RetrievedChunk> chunks = hybridSearchService.search(userQuery, k);
        String context = formatContext(chunks);
        Map<String, String> variables = Map.of(
                "context", context,
                "query", userQuery
        );
        String answer = chatAgentRunner.run(TEAM, AGENT, TASK, variables);
        return new RagResponse(answer, chunks.stream()
                .map(c -> new RagResponse.Source(c.getSourceName(), c.getDocumentId(), c.getText()))
                .collect(Collectors.toList()));
    }

    private String formatContext(List<RetrievedChunk> chunks) {
        if (chunks.isEmpty()) return "(No relevant documents found.)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk c = chunks.get(i);
            sb.append("[Doc: ").append(c.getSourceName()).append("]\n").append(c.getText()).append("\n\n");
        }
        return sb.toString();
    }

    public static class RagResponse {
        private String answer;
        private List<Source> sources;

        public RagResponse() {}
        public RagResponse(String answer, List<Source> sources) {
            this.answer = answer;
            this.sources = sources;
        }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public List<Source> getSources() { return sources; }
        public void setSources(List<Source> sources) { this.sources = sources; }

        public static class Source {
            private String sourceName;
            private String documentId;
            private String excerpt;

            public Source() {}
            public Source(String sourceName, String documentId, String excerpt) {
                this.sourceName = sourceName;
                this.documentId = documentId;
                this.excerpt = excerpt;
            }
            public String getSourceName() { return sourceName; }
            public void setSourceName(String sourceName) { this.sourceName = sourceName; }
            public String getDocumentId() { return documentId; }
            public void setDocumentId(String documentId) { this.documentId = documentId; }
            public String getExcerpt() { return excerpt; }
            public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
        }
    }
}
