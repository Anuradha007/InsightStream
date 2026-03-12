package com.insightstream.config;

/**
 * Configuration for a task (user prompt template and optional structured output).
 */
public class TaskConfig {

    private String userPrompt = "";
    private String useStructuredOutput;

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }
    public String getUseStructuredOutput() { return useStructuredOutput; }
    public void setUseStructuredOutput(String useStructuredOutput) { this.useStructuredOutput = useStructuredOutput; }
}
