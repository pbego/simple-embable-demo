package com.example.simpledemo.memory;

import java.time.Instant;

/** Summary row for the {@code conversations} shell command. */
public record ConversationSummary(String id, Instant updatedAt, String preview) {}
