package com.example.simpledemo.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConversationSummarizationPlannerTest {

  @Test
  void emptyWhenWithinWindow() {
    var messages = List.<com.embabel.chat.Message>of(new UserMessage("one"));
    assertTrue(
        ConversationSummarizationPlanner.messagesToSummarize(
                messages, ConversationMemoryState.NONE_SUMMARIZED, 20)
            .isEmpty());
  }

  @Test
  void selectsMessagesBetweenSummaryCursorAndRecentWindow() {
    List<Message> messages = new ArrayList<>();
    messages.add(new UserMessage("m0"));
    messages.add(new AssistantMessage("m1"));
    messages.add(new UserMessage("m2"));
    messages.add(new AssistantMessage("m3"));
    messages.add(new UserMessage("m4"));
    messages.add(new AssistantMessage("m5"));
    var slice =
        ConversationSummarizationPlanner.messagesToSummarize(
                messages, ConversationMemoryState.NONE_SUMMARIZED, 2)
            .orElseThrow();
    assertEquals(4, slice.messages().size());
    assertEquals("m0", slice.messages().getFirst().getContent());
    assertEquals("m3", slice.messages().get(3).getContent());
    assertEquals(3, slice.newSummarizedThroughIndex());
  }

  @Test
  void incrementalSliceAfterPriorSummary() {
    List<Message> messages = new ArrayList<>();
    messages.add(new UserMessage("m0"));
    messages.add(new AssistantMessage("m1"));
    messages.add(new UserMessage("m2"));
    messages.add(new AssistantMessage("m3"));
    messages.add(new UserMessage("m4"));
    messages.add(new AssistantMessage("m5"));
    messages.add(new UserMessage("m6"));
    var slice =
        ConversationSummarizationPlanner.messagesToSummarize(messages, 3, 2).orElseThrow();
    assertEquals(1, slice.messages().size());
    assertEquals("m4", slice.messages().getFirst().getContent());
    assertEquals(4, slice.newSummarizedThroughIndex());
  }
}
