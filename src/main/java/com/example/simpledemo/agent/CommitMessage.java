package com.example.simpledemo.agent;

/**
 * Suggested commit message in conventional-commit style.
 */
public record CommitMessage(String subject, String body) {

  /** Human-readable text for chat / shell output. */
  public String formatted() {
    var line = subject != null ? subject.trim() : "";
    if (line.isBlank()) {
      return "(no subject generated)";
    }
    if (body == null || body.isBlank()) {
      return line;
    }
    return "Subject: %s%n%n%s".formatted(line, body.trim());
  }
}
