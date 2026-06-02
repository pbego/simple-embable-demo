package com.example.simpledemo.security;

import com.embabel.agent.api.validation.guardrails.UserInputGuardRail;
import com.embabel.agent.core.Blackboard;
import com.embabel.chat.UserMessage;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;
import com.example.simpledemo.audit.AuditRecorder;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Blocks obviously dangerous git instructions in user prompts.
 */
@Component
public class CommitSafetyGuardRail implements UserInputGuardRail {

  private final AuditRecorder auditRecorder;

  public CommitSafetyGuardRail(AuditRecorder auditRecorder) {
    this.auditRecorder = auditRecorder;
  }

  @Override
  public String getName() {
    return "commit-safety";
  }

  @Override
  public String getDescription() {
    return "Blocks mutating git commands in user prompts";
  }

  private static final Pattern DANGEROUS =
      Pattern.compile(
          "git\\s+(push|commit|reset|checkout|merge|rebase|clean|stash\\s+drop)",
          Pattern.CASE_INSENSITIVE);

  @Override
  public ValidationResult validate(String content, Blackboard blackboard) {
    var combined = content == null ? "" : content.toLowerCase(Locale.ROOT);
    if (DANGEROUS.matcher(combined).find()) {
      return invalid(null, "Refusing prompt that requests mutating git commands (read-only demo).");
    }
    if (combined.contains("force push") || combined.contains("--force")) {
      return invalid(null, "Force push is not allowed in this demo.");
    }
    return ValidationResult.Companion.getVALID();
  }

  @Override
  public ValidationResult validate(List<UserMessage> messages, Blackboard blackboard) {
    return validate(combineMessages(messages), blackboard);
  }

  private ValidationResult invalid(String conversationId, String message) {
    auditRecorder.guardrailBlocked(conversationId, getName(), message);
    return new ValidationResult(
        false,
        List.of(new ValidationError("commit-safety", message, ValidationSeverity.CRITICAL)));
  }
}
