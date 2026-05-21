package com.example.simpledemo.memory;

/**
 * Rolling summary of conversation turns that no longer fit in the recent message window.
 */
public record ConversationMemoryState(String sessionSummary, int summarizedThroughIndex) {

  public static final int NONE_SUMMARIZED = -1;

  public ConversationMemoryState {
    if (sessionSummary != null && sessionSummary.isBlank()) {
      sessionSummary = null;
    }
    if (summarizedThroughIndex < NONE_SUMMARIZED) {
      summarizedThroughIndex = NONE_SUMMARIZED;
    }
  }

  public static ConversationMemoryState empty() {
    return new ConversationMemoryState(null, NONE_SUMMARIZED);
  }

  public boolean hasSummary() {
    return sessionSummary != null && !sessionSummary.isBlank();
  }
}
