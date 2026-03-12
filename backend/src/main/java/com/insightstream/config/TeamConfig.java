package com.insightstream.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for a team: description, agents, and tasks.
 */
public class TeamConfig {

    private String description = "";
    private Map<String, AgentConfig> agents = new LinkedHashMap<>();
    private Map<String, TaskConfig> tasks = new LinkedHashMap<>();

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, AgentConfig> getAgents() { return agents; }
    public void setAgents(Map<String, AgentConfig> agents) { this.agents = agents; }
    public Map<String, TaskConfig> getTasks() { return tasks; }
    public void setTasks(Map<String, TaskConfig> tasks) { this.tasks = tasks; }
}
