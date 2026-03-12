package com.insightstream.config;

/**
 * Configuration for a single agent: provider, model, and prompt settings.
 */
public class AgentConfig {

    private int maxTokens = 4096;
    private String model;
    private String provider = "ollama";
    private double temperature = 0.7;
    private String reasoningEffort = "medium";
    private int maxTurns = 10;
    private String systemPrompt = "";

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public String getReasoningEffort() { return reasoningEffort; }
    public void setReasoningEffort(String reasoningEffort) { this.reasoningEffort = reasoningEffort; }
    public int getMaxTurns() { return maxTurns; }
    public void setMaxTurns(int maxTurns) { this.maxTurns = maxTurns; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
}
