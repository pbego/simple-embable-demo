package com.example.simpledemo.config;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.Verbosity;
import com.embabel.chat.Chatbot;
import com.embabel.chat.agent.AgentProcessChatbot;
import com.embabel.chat.ConversationFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires Embabel {@link Chatbot} so shell {@code chat} uses platform utility actions, including
 * {@link com.example.simpledemo.chat.ChatRouter}, with persisted conversation history.
 */
@Configuration
public class DemoChatConfiguration {

  @Bean
  Chatbot chatbot(AgentPlatform agentPlatform, ConversationFactory conversationFactory) {
    return AgentProcessChatbot.utilityFromPlatform(
        agentPlatform, conversationFactory, new Verbosity().showPrompts());
  }
}
