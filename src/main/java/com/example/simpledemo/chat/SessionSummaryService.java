package com.example.simpledemo.chat;

import com.embabel.agent.api.common.OperationContext;
import com.embabel.chat.Conversation;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import com.example.simpledemo.memory.ConversationMemoryAccessor;
import com.example.simpledemo.memory.ConversationMemoryState;
import com.example.simpledemo.memory.ConversationMemoryProperties;
import com.example.simpledemo.memory.ConversationSummarizationPlanner;
import com.example.simpledemo.memory.ConversationSummarizationPlanner.SummarizationSlice;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SessionSummaryService {

  private static final String SUMMARIZE_PROMPT =
      """
      You maintain a rolling session summary for a developer chat about git and commits.
      Update the summary using the existing summary (if any) and the new messages below.
      Preserve: topics discussed, decisions, repo context, commit wording, and open questions.
      Omit greetings and filler. Write concise prose (under 300 words).
      Return only the updated summary text, with no preamble.

      Existing summary:
      %s

      New messages to fold in:
      %s
      """
          .strip();

  private final ConversationMemoryProperties properties;

  public SessionSummaryService(ConversationMemoryProperties properties) {
    this.properties = properties;
  }

  /**
   * When the transcript exceeds the recent window, summarize messages that fell out of the window
   * and persist the result on the conversation.
   */
  public void refreshSummaryIfNeeded(Conversation conversation, OperationContext context) {
    if (!properties.memorySummarizationEnabled()) {
      return;
    }
    if (!(conversation instanceof ConversationMemoryAccessor accessor)) {
      return;
    }
    var slice =
        ConversationSummarizationPlanner.messagesToSummarize(
                conversation.getMessages(),
                accessor.memoryState().summarizedThroughIndex(),
                properties.memoryMaxMessages())
            .orElse(null);
    if (slice == null) {
      return;
    }
    var existing = accessor.memoryState().sessionSummary();
    var transcript = formatTranscript(slice.messages());
    var prompt = SUMMARIZE_PROMPT.formatted(existing != null ? existing : "(none)", transcript);
    var summary =
        context.ai().withDefaultLlm().createObject(List.of(new UserMessage(prompt)), String.class);
    accessor.updateMemoryState(
        new ConversationMemoryState(summary.strip(), slice.newSummarizedThroughIndex()));
  }

  private static String formatTranscript(List<Message> messages) {
    var builder = new StringBuilder();
    for (var message : messages) {
      builder.append(message.getRole().name().toLowerCase());
      builder.append(": ");
      builder.append(message.getContent().strip());
      builder.append('\n');
    }
    return builder.toString().strip();
  }
}
