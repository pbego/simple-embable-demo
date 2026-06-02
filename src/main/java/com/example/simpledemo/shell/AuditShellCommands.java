package com.example.simpledemo.shell;

import com.example.simpledemo.audit.AuditEvent;
import com.example.simpledemo.audit.AuditRepository;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@Profile("postgres")
public class AuditShellCommands {

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  private final AuditRepository auditRepository;

  public AuditShellCommands(AuditRepository auditRepository) {
    this.auditRepository = auditRepository;
  }

  @ShellMethod(value = "Show audit events for a conversation", key = {"audit-tail"})
  public String auditTail(
      @ShellOption(help = "Conversation id") String conversationId,
      @ShellOption(defaultValue = "50", help = "Maximum events to show") int limit) {
    var events = auditRepository.findByConversationId(conversationId, limit);
    if (events.isEmpty()) {
      return "No audit events for conversation '" + conversationId + "'.";
    }
    return events.stream().map(this::formatEvent).collect(Collectors.joining("\n"));
  }

  private String formatEvent(AuditEvent event) {
    return TIME_FORMAT.format(event.createdAt())
        + "  "
        + event.eventType()
        + "  "
        + event.payload();
  }
}
