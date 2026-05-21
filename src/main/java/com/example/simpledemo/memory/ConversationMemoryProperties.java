package com.example.simpledemo.memory;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simple-demo")
public record ConversationMemoryProperties(
    String conversationsDir, int memoryMaxMessages, boolean memorySummarizationEnabled) {

  public ConversationMemoryProperties {
    if (conversationsDir == null || conversationsDir.isBlank()) {
      conversationsDir = System.getProperty("user.home") + "/.simple-demo/conversations";
    }
    if (memoryMaxMessages <= 0) {
      memoryMaxMessages = 20;
    }
  }
}
