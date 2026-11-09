package com.example.simpledemo.shell;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.shell.TerminalServices;
import com.embabel.agent.spi.logging.ColorPalette;
import com.embabel.chat.ChatSession;
import com.embabel.chat.Chatbot;
import com.example.simpledemo.memory.ConversationStore;
import com.example.simpledemo.memory.ConversationSummary;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

/**
 * Shell commands for listing and resuming persisted conversations.
 *
 * <p>The built-in Embabel {@code chat} command starts a new session (using the injected
 * {@link Chatbot} bean). Use {@code chat --resume <id>} here to continue a saved conversation.
 */
@Component
public class ConversationShellCommands {

  private static final DateTimeFormatter UPDATED_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

  private final Chatbot chatbot;
  private final AgentPlatform agentPlatform;
  private final TerminalServices terminalServices;
  private final ConversationStore store;
  private final ColorPalette colorPalette;

  public ConversationShellCommands(
      Chatbot chatbot,
      AgentPlatform agentPlatform,
      TerminalServices terminalServices,
      ConversationStore store,
      ColorPalette colorPalette) {
    this.chatbot = chatbot;
    this.agentPlatform = agentPlatform;
    this.terminalServices = terminalServices;
    this.store = store;
    this.colorPalette = colorPalette;
  }

  @Command(
      name = "conversations",
      alias = "conv-list",
      description = "List saved chat conversations")
  public String conversations() {
    var summaries = store.list();
    if (summaries.isEmpty()) {
      return "No saved conversations in " + store.storageDescription();
    }
    var header = String.format("%-10s  %-16s  %s", "ID", "UPDATED", "PREVIEW");
    var rows =
        summaries.stream()
            .map(this::formatRow)
            .collect(Collectors.joining("\n"));
    return header + "\n" + rows + "\n\nStorage: " + store.storageDescription();
  }

  @Command(
      name = "resume-chat",
      alias = "chat-resume",
      description = "Resume a saved chat conversation (use 'chat' to start a new one)")
  public String resumeChat(
      @Argument(index = 0, description = "Conversation id from 'conversations'")
          String conversationId) {
    if (store.load(conversationId).isEmpty()) {
      return "No conversation found with id '" + conversationId + "'. Run 'conversations' to list saved ids.";
    }

    ChatSession session =
        chatbot.createSession(
            null,
            terminalServices.outputChannel(agentPlatform),
            null,
            conversationId);

    var welcome =
        "Resumed conversation "
            + conversationId
            + " ("
            + session.getConversation().getMessages().size()
            + " messages). Storage: "
            + store.storageDescription()
            + ".";

    return terminalServices.chat(session, welcome, colorPalette);
  }

  private String formatRow(ConversationSummary summary) {
    return String.format(
        "%-10s  %-16s  %s",
        summary.id(),
        UPDATED_FORMAT.format(summary.updatedAt()),
        summary.preview());
  }
}
