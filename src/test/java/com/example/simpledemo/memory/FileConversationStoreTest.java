package com.example.simpledemo.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.chat.AssistantMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileConversationStoreTest {

  @TempDir Path tempDir;

  @Test
  void saveLoadAndListSurvivesNewStoreInstance() {
    var properties = new ConversationMemoryProperties(tempDir.toString(), 20, true);
    var store1 = new FileConversationStore(properties);
    var conversation = new InMemoryConversation(java.util.List.of(), "test-conv", true);
    conversation.addMessage(new UserMessage("Hello"));
    conversation.addMessage(new AssistantMessage("Hi there"));

    store1.save(conversation);

    var store2 = new FileConversationStore(properties);
    var loaded = store2.load("test-conv");
    assertTrue(loaded.isPresent());
    assertEquals(2, loaded.get().getMessages().size());
    assertEquals("Hello", loaded.get().getMessages().getFirst().getContent());

    var summaries = store2.list();
    assertEquals(1, summaries.size());
    assertEquals("test-conv", summaries.getFirst().id());
    assertTrue(summaries.getFirst().preview().contains("Hello"));

    assertTrue(Files.exists(tempDir.resolve("test-conv.json")));
  }

  @Test
  void loadReturnsEmptyWhenMissing() {
    var store = new FileConversationStore(new ConversationMemoryProperties(tempDir.toString(), 20, true));
    assertFalse(store.load("missing").isPresent());
  }

  @Test
  void listIsEmptyForMissingDirectory() {
    var missing = tempDir.resolve("no-such-dir");
    var store = new FileConversationStore(new ConversationMemoryProperties(missing.toString(), 20, true));
    assertTrue(store.list().isEmpty());
  }

  @Test
  void saveLoadRoundTripsSessionSummary() {
    var properties = new ConversationMemoryProperties(tempDir.toString(), 20, true);
    var store = new FileConversationStore(properties);
    var conversation = new InMemoryConversation(java.util.List.of(), "sum-conv", true);
    var persisting =
        new PersistingConversation(
            conversation,
            store,
            new ConversationMemoryState("Discussed conventional commits.", 3));

    persisting.addMessage(new UserMessage("follow up"));

    var loaded = store.load("sum-conv").orElseThrow();
    assertEquals("Discussed conventional commits.", loaded.memoryState().sessionSummary());
    assertEquals(3, loaded.memoryState().summarizedThroughIndex());
    assertEquals(1, loaded.getMessages().size());
  }
}
