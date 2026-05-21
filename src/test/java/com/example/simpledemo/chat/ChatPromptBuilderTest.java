package com.example.simpledemo.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.chat.AssistantMessage;
import com.embabel.chat.SystemMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import com.example.simpledemo.memory.ConversationMemoryState;
import org.junit.jupiter.api.Test;

class ChatPromptBuilderTest {

  private final ChatPromptBuilder builder = new ChatPromptBuilder();

  @Test
  void includesSummaryAndRecentMessages() {
    var conversation =
        new InMemoryConversation(
            java.util.List.of(
                new UserMessage("old"),
                new AssistantMessage("old reply"),
                new UserMessage("recent"),
                new AssistantMessage("recent reply")),
            "c1",
            false);
    var memory = new ConversationMemoryState("User asked about commits.", 1);

    var messages = builder.build("Base system", conversation, memory, 2);

    assertEquals(4, messages.size());
    assertEquals("Base system", messages.get(0).getContent());
    assertTrue(messages.get(1) instanceof SystemMessage);
    assertTrue(messages.get(1).getContent().contains("User asked about commits"));
    assertEquals("recent", messages.get(2).getContent());
    assertEquals("recent reply", messages.get(3).getContent());
  }
}
