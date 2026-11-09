package com.example.simpledemo.shell;

import com.example.simpledemo.audit.AuditEvent;
import com.example.simpledemo.audit.AuditRepository;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@Profile("postgres")
public class AuditShellCommands {

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  private final AuditRepository auditRepository;

  public AuditShellCommands(AuditRepository auditRepository) {
    this.auditRepository = auditRepository;
  }

  @Command(name = "audit-tail", description = "Show audit events for a conversation")
  public String auditTail(
      @Argument(index = 0, description = "Conversation id") String conversationId,
      @Option(longName = "limit", defaultValue = "50", description = "Maximum events to show") int limit) {
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
