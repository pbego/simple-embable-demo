package com.example.simpledemo.memory;

/**
 * Optional session summary metadata attached to a persisted {@link com.embabel.chat.Conversation}.
 */
public interface ConversationMemoryAccessor {

  ConversationMemoryState memoryState();

  void updateMemoryState(ConversationMemoryState state);
}
