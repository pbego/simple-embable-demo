package com.example.simpledemo.config;

import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.Verbosity;
import com.embabel.chat.Chatbot;
import com.embabel.chat.agent.AgentProcessChatbot;
import com.embabel.chat.agent.AgentSource;
import com.embabel.chat.agent.ListenerProvider;
import com.example.simpledemo.chat.CommitChatAgent;
import com.example.simpledemo.memory.FileConversationFactory;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfiguration {

  public static final String CHAT_AGENT_NAME = "Commit chat";

  @Bean
  Chatbot chatbot(AgentPlatform agentPlatform, FileConversationFactory factory) {
    AgentSource agentSource =
        user ->
            agentPlatform.agents().stream()
                .filter(agent -> CHAT_AGENT_NAME.equals(agent.getName()))
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "Agent '"
                                + CHAT_AGENT_NAME
                                + "' not found. Expected "
                                + CommitChatAgent.class.getSimpleName()));
    ListenerProvider listeners = (user, outputChannel) -> Collections.emptyList();
    return new AgentProcessChatbot(
        agentPlatform,
        agentSource,
        factory,
        listeners,
        PlannerType.UTILITY,
        new Verbosity());
  }
}
