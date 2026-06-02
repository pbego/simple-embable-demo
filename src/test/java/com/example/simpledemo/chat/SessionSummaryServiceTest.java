package com.example.simpledemo.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import com.example.simpledemo.memory.ConversationMemoryProperties;
import com.example.simpledemo.memory.ConversationMemoryState;
import com.example.simpledemo.audit.AuditRecorder;
import com.example.simpledemo.audit.NoOpAuditRepository;
import com.example.simpledemo.memory.FileConversationStore;
import com.example.simpledemo.memory.PersistingConversation;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SessionSummaryServiceTest {

  @TempDir Path tempDir;

  @Test
  void skipsWhenWithinWindow() {
    var properties = new ConversationMemoryProperties(tempDir.toString(), 20, true);
    var service = new SessionSummaryService(properties, new AuditRecorder(new NoOpAuditRepository()));
    var store = new FileConversationStore(properties);
    var conversation =
        new PersistingConversation(new InMemoryConversation(java.util.List.of(), "c1", true), store);
    conversation.addMessage(new UserMessage("only one"));
    var context = FakeOperationContext.create();

    service.refreshSummaryIfNeeded(conversation, context);

    assertTrue(context.getLlmInvocations().isEmpty());
    assertEquals(ConversationMemoryState.NONE_SUMMARIZED, conversation.memoryState().summarizedThroughIndex());
  }

  @Test
  void summarizesDroppedMessagesAndPersists() {
    var properties = new ConversationMemoryProperties(tempDir.toString(), 2, true);
    var service = new SessionSummaryService(properties, new AuditRecorder(new NoOpAuditRepository()));
    var store = new FileConversationStore(properties);
    var messages = new ArrayList<Message>();
    messages.add(new UserMessage("topic: conventional commits"));
    messages.add(new AssistantMessage("use feat: prefix"));
    messages.add(new UserMessage("shorten the subject"));
    messages.add(new AssistantMessage("try feat: shorten subject"));
    var conversation =
        new PersistingConversation(new InMemoryConversation(messages, "sum-test", true), store);
    var context = FakeOperationContext.create();
    context.expectResponse("User wants conventional commits; assistant suggested feat: prefix.");

    service.refreshSummaryIfNeeded(conversation, context);

    assertEquals(1, context.getLlmInvocations().size());
    assertTrue(context.getLlmInvocations().getFirst().getPrompt().contains("conventional commits"));
    assertEquals(
        "User wants conventional commits; assistant suggested feat: prefix.",
        conversation.memoryState().sessionSummary());
    assertEquals(1, conversation.memoryState().summarizedThroughIndex());

    var reloaded = store.load("sum-test").orElseThrow();
    assertEquals(conversation.memoryState().sessionSummary(), reloaded.memoryState().sessionSummary());
    assertEquals(1, reloaded.memoryState().summarizedThroughIndex());
  }

  @Test
  void disabledByProperty() {
    var properties = new ConversationMemoryProperties(tempDir.toString(), 2, false);
    var service = new SessionSummaryService(properties, new AuditRecorder(new NoOpAuditRepository()));
    var store = new FileConversationStore(properties);
    var messages = new ArrayList<Message>();
    messages.add(new UserMessage("a"));
    messages.add(new AssistantMessage("b"));
    messages.add(new UserMessage("c"));
    messages.add(new AssistantMessage("d"));
    var conversation =
        new PersistingConversation(new InMemoryConversation(messages, "off", true), store);
    var context = FakeOperationContext.create();

    service.refreshSummaryIfNeeded(conversation, context);

    assertTrue(context.getLlmInvocations().isEmpty());
  }
}
