package com.example.simpledemo.config;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.Verbosity;
import com.embabel.chat.Chatbot;
import com.embabel.chat.agent.AgentProcessChatbot;
import com.embabel.chat.support.InMemoryConversationFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires Embabel {@link Chatbot} so shell {@code chat} uses platform utility actions, including
 * {@link com.example.simpledemo.chat.ChatRouter}.
 */
@Configuration
public class DemoChatConfiguration {

  @Bean
  Chatbot chatbot(AgentPlatform agentPlatform) {
    return AgentProcessChatbot.utilityFromPlatform(
        agentPlatform, new InMemoryConversationFactory(), new Verbosity().showPrompts());
  }
}
