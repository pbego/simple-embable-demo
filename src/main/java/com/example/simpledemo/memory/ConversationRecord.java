package com.example.simpledemo.memory;

import java.time.Instant;
import java.util.List;

/**
 * JSON-serializable conversation snapshot written to disk.
 */
public record ConversationRecord(
    String id,
    String title,
    Instant updatedAt,
    List<StoredMessageRecord> messages,
    String sessionSummary,
    int summarizedThroughIndex) {

  /** Backward-compatible constructor for records without summary fields. */
  public ConversationRecord(
      String id, String title, Instant updatedAt, List<StoredMessageRecord> messages) {
    this(id, title, updatedAt, messages, null, ConversationMemoryState.NONE_SUMMARIZED);
  }
}
