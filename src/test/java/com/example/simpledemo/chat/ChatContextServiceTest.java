package com.example.simpledemo.chat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.chat.AssistantMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import com.example.simpledemo.memory.ConversationMemoryProperties;
import com.example.simpledemo.memory.ConversationMemoryState;
import com.example.simpledemo.memory.FileConversationStore;
import com.example.simpledemo.memory.PersistingConversation;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChatContextServiceTest {

  @TempDir Path tempDir;

  @Test
  void enrichQuestionIncludesSummaryAndRecentTurns() {
    var properties = new ConversationMemoryProperties(tempDir.toString(), 20, true);
    var store = new FileConversationStore(properties);
    var conversation =
        new PersistingConversation(
            new InMemoryConversation(java.util.List.of(), "ctx", true),
            store,
            new ConversationMemoryState("Discussed release branches.", 2));
    conversation.addMessage(new UserMessage("earlier question"));
    conversation.addMessage(new AssistantMessage("earlier answer"));
    conversation.addMessage(new UserMessage("follow up"));

    var service = new ChatContextService(new ChatPromptBuilder(), properties);
    var enriched = service.enrichQuestion(conversation, "follow up");

    assertTrue(enriched.contains("Discussed release branches."));
    assertTrue(enriched.contains("earlier question"));
    assertTrue(enriched.contains("Current request: follow up"));
  }
}
