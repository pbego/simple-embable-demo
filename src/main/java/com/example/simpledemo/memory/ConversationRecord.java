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
    List<StoredMessageRecord> messages) {}
