package com.example.simpledemo.shell;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.shell.TerminalServices;
import com.embabel.agent.spi.logging.ColorPalette;
import com.embabel.chat.Chatbot;
import com.embabel.chat.ChatSession;
import com.example.simpledemo.memory.ConversationSummary;
import com.example.simpledemo.memory.FileConversationStore;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell commands for listing and resuming persisted conversations.
 *
 * <p>The built-in Embabel {@code chat} command starts a new session (using the injected
 * {@link Chatbot} bean). Use {@code chat --resume <id>} here to continue a saved conversation.
 */
@ShellComponent
public class ConversationShellCommands {

  private static final DateTimeFormatter UPDATED_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

  private final Chatbot chatbot;
  private final AgentPlatform agentPlatform;
  private final TerminalServices terminalServices;
  private final FileConversationStore store;
  private final ColorPalette colorPalette;

  public ConversationShellCommands(
      Chatbot chatbot,
      AgentPlatform agentPlatform,
      TerminalServices terminalServices,
      FileConversationStore store,
      ColorPalette colorPalette) {
    this.chatbot = chatbot;
    this.agentPlatform = agentPlatform;
    this.terminalServices = terminalServices;
    this.store = store;
    this.colorPalette = colorPalette;
  }

  @ShellMethod(key = {"conversations", "conv-list"}, value = "List saved chat conversations")
  public String conversations() {
    var summaries = store.list();
    if (summaries.isEmpty()) {
      return "No saved conversations in " + store.conversationsDir();
    }
    var header = String.format("%-10s  %-16s  %s", "ID", "UPDATED", "PREVIEW");
    var rows =
        summaries.stream()
            .map(this::formatRow)
            .collect(Collectors.joining("\n"));
    return header + "\n" + rows + "\n\nDirectory: " + store.conversationsDir();
  }

  @ShellMethod(
      value = "Resume a saved chat conversation (use 'chat' to start a new one)",
      key = {"chat-resume", "resume-chat"})
  public String resumeChat(
      @ShellOption(help = "Conversation id from 'conversations'") String conversationId) {
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
            + " messages). Saved under "
            + store.conversationsDir()
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
