package com.insightstream.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root configuration for all teams. Bound from application.yml: insightstream.team-configuration
 * is the map (keys = team names e.g. QueryTeam, IngestionTeam).
 */
@ConfigurationProperties(prefix = "insightstream")
public class TeamConfigurationProperties {

    /** Map of team name -> team config; bound from YAML key "team-configuration". */
    private Map<String, TeamConfig> teamConfiguration = new LinkedHashMap<>();

    public Map<String, TeamConfig> getTeamConfiguration() { return teamConfiguration; }
    public void setTeamConfiguration(Map<String, TeamConfig> teamConfiguration) { this.teamConfiguration = teamConfiguration; }

    public TeamConfig getTeam(String teamName) {
        return teamConfiguration.get(teamName);
    }

    public AgentConfig getAgent(String teamName, String agentName) {
        TeamConfig team = getTeam(teamName);
        if (team == null) return null;
        return team.getAgents().get(agentName);
    }

    public TaskConfig getTask(String teamName, String taskName) {
        TeamConfig team = getTeam(teamName);
        if (team == null) return null;
        return team.getTasks().get(taskName);
    }
}
