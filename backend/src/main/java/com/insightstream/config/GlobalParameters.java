package com.insightstream.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global parameters for chunking, embedding, and default model providers.
 * Bound from application.yml under insightstream.global-parameters.
 */
@ConfigurationProperties(prefix = "insightstream.global-parameters")
public class GlobalParameters {

    private int chunkSize = 1000;
    private int chunkOverlap = 200;

    private String agentSummaryProvider = "ollama";
    private String agentSummaryModel = "llama3.2:latest";
    private String chunkContextProvider = "ollama";
    private String chunkContextModel = "llama3.2:latest";
    private String memorySummaryProvider = "ollama";
    private String memorySummaryModel = "llama3.2:latest";

    private String embeddingProvider = "ollama";
    private String embeddingModel = "nomic-embed-text:latest";

    private String queryAgentProvider = "ollama";
    private String queryAgentModel = "llama3.2:latest";

    private int anthropicTimeoutSeconds = 1200;

    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
    public int getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
    public String getAgentSummaryProvider() { return agentSummaryProvider; }
    public void setAgentSummaryProvider(String agentSummaryProvider) { this.agentSummaryProvider = agentSummaryProvider; }
    public String getAgentSummaryModel() { return agentSummaryModel; }
    public void setAgentSummaryModel(String agentSummaryModel) { this.agentSummaryModel = agentSummaryModel; }
    public String getChunkContextProvider() { return chunkContextProvider; }
    public void setChunkContextProvider(String chunkContextProvider) { this.chunkContextProvider = chunkContextProvider; }
    public String getChunkContextModel() { return chunkContextModel; }
    public void setChunkContextModel(String chunkContextModel) { this.chunkContextModel = chunkContextModel; }
    public String getMemorySummaryProvider() { return memorySummaryProvider; }
    public void setMemorySummaryProvider(String memorySummaryProvider) { this.memorySummaryProvider = memorySummaryProvider; }
    public String getMemorySummaryModel() { return memorySummaryModel; }
    public void setMemorySummaryModel(String memorySummaryModel) { this.memorySummaryModel = memorySummaryModel; }
    public String getEmbeddingProvider() { return embeddingProvider; }
    public void setEmbeddingProvider(String embeddingProvider) { this.embeddingProvider = embeddingProvider; }
    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    public String getQueryAgentProvider() { return queryAgentProvider; }
    public void setQueryAgentProvider(String queryAgentProvider) { this.queryAgentProvider = queryAgentProvider; }
    public String getQueryAgentModel() { return queryAgentModel; }
    public void setQueryAgentModel(String queryAgentModel) { this.queryAgentModel = queryAgentModel; }
    public int getAnthropicTimeoutSeconds() { return anthropicTimeoutSeconds; }
    public void setAnthropicTimeoutSeconds(int anthropicTimeoutSeconds) { this.anthropicTimeoutSeconds = anthropicTimeoutSeconds; }
}
