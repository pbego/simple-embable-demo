package com.example.simpledemo.memory;

import java.time.Instant;

/**
 * JSON-serializable message for conversation persistence.
 */
public record StoredMessageRecord(String role, String content, Instant timestamp) {}
