package com.example.simpledemo.audit;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
    String conversationId, String eventType, Map<String, Object> payload, Instant createdAt) {

  public AuditEvent(String conversationId, String eventType, Map<String, Object> payload) {
    this(conversationId, eventType, payload, Instant.now());
  }
}
