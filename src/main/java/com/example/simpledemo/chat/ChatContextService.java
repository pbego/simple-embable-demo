package com.example.simpledemo.chat;

import com.embabel.chat.Conversation;
import com.embabel.chat.Message;
import com.embabel.chat.SystemMessage;
import com.example.simpledemo.memory.ConversationMemoryAccessor;
import com.example.simpledemo.memory.ConversationMemoryProperties;
import com.example.simpledemo.memory.ConversationMemoryState;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Builds short-term LLM context: rolling summary plus a sliding window of recent messages.
 */
@Service
public class ChatContextService {

  private static final String ROUTING_SYSTEM_PROMPT =
      """
      You route chat messages to specialist agents.

      Agents:
      - greet: hellos, small talk, unclear/general chat
      - joke: humor, jokes, funny requests
      - commit: git commit messages, diffs, staged/unstaged changes, conventional commits
      - style: commit conventions, style guide, how we format commits (not generating a message)

      If the user asks for multiple things in one message (e.g. commit message AND a joke),
      include every matching agent in targets (e.g. ["commit", "joke"]).
      If only one applies, return a one-element targets list.

      Return JSON: targets (array of greet|joke|commit|style strings), rationale (short string).
      Legacy field target (single string) is allowed if targets is omitted.
      """
          .strip();

  private final ChatPromptBuilder chatPromptBuilder;
  private final ConversationMemoryProperties properties;

  public ChatContextService(
      ChatPromptBuilder chatPromptBuilder, ConversationMemoryProperties properties) {
    this.chatPromptBuilder = chatPromptBuilder;
    this.properties = properties;
  }

  public List<Message> routingMessages(Conversation conversation) {
    if (!(conversation instanceof ConversationMemoryAccessor accessor)) {
      return List.of(new SystemMessage(ROUTING_SYSTEM_PROMPT));
    }
    return chatPromptBuilder.build(
        ROUTING_SYSTEM_PROMPT,
        conversation,
        accessor.memoryState(),
        properties.memoryMaxMessages());
  }

  /**
   * Enriches the current user question with session summary and recent turns for specialist agents.
   */
  public String enrichQuestion(Conversation conversation, String question) {
    var q = question != null ? question : "";
    if (!(conversation instanceof ConversationMemoryAccessor accessor)) {
      return q;
    }
    return enrichQuestion(q, accessor.memoryState(), conversation);
  }

  private String enrichQuestion(
      String question, ConversationMemoryState memory, Conversation conversation) {
    var builder = new StringBuilder();
    if (memory.hasSummary()) {
      builder.append("Session summary:\n");
      builder.append(memory.sessionSummary().strip());
      builder.append("\n\n");
    }
    var recentCount = Math.min(6, properties.memoryMaxMessages());
    var recent = conversation.last(recentCount).getMessages();
    if (recent.size() > 1) {
      builder.append("Recent conversation:\n");
      var end = recent.size() - 1;
      for (var index = 0; index < end; index++) {
        var message = recent.get(index);
        builder.append(message.getRole().name().toLowerCase());
        builder.append(": ");
        builder.append(message.getContent().strip());
        builder.append('\n');
      }
      builder.append('\n');
    }
    builder.append("Current request: ");
    builder.append(question.strip());
    return builder.toString();
  }
}
