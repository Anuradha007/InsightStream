package com.insightstream.agent;

import com.insightstream.config.AgentConfig;
import com.insightstream.config.TaskConfig;
import com.insightstream.config.TeamConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runs a configured agent (system + user prompt) using the default ChatModel.
 * Prompt templates can contain placeholders like {context}, {query}.
 */
@Component
public class ChatAgentRunner {

    private static final Logger log = LoggerFactory.getLogger(ChatAgentRunner.class);

    public ChatAgentRunner(ChatModel chatModel, TeamConfigurationProperties teamConfiguration) {
        this.chatModel = chatModel;
        this.teamConfiguration = teamConfiguration;
    }
    private final ChatModel chatModel;
    private final TeamConfigurationProperties teamConfiguration;

    /**
     * Run the given team's agent and task with the provided variable bindings.
     *
     * @param teamName   e.g. "QueryTeam"
     * @param agentName  e.g. "query_agent"
     * @param taskName   e.g. "answer"
     * @param variables  map of placeholder name -> value (e.g. "context" -> "...", "query" -> "...")
     * @return model response content
     */
    public String run(String teamName, String agentName, String taskName, Map<String, String> variables) {
        AgentConfig agentConfig = teamConfiguration.getAgent(teamName, agentName);
        TaskConfig taskConfig = teamConfiguration.getTask(teamName, taskName);
        if (agentConfig == null || taskConfig == null) {
            log.warn("Missing agent or task config: team={}, agent={}, task={}", teamName, agentName, taskName);
            return "Configuration error: agent or task not found.";
        }

        String systemPrompt = replacePlaceholders(agentConfig.getSystemPrompt(), variables);
        String userPrompt = replacePlaceholders(taskConfig.getUserPrompt(), variables);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userPrompt));

        Prompt prompt = new Prompt(messages);
        var response = chatModel.call(prompt);
        return response.getResult() != null ? response.getResult().getOutput().getContent() : "";
    }

    private String replacePlaceholders(String template, Map<String, String> variables) {
        if (template == null || variables == null) return template == null ? "" : template;
        String out = template;
        for (Map.Entry<String, String> e : variables.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue() != null ? e.getValue() : "");
        }
        return out;
    }
}
