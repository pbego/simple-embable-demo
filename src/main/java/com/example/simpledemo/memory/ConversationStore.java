package com.example.simpledemo.memory;

import com.embabel.chat.Conversation;
import java.util.List;
import java.util.Optional;

/** Persists and loads {@link Conversation} instances (file or database). */
public interface ConversationStore {

  void save(Conversation conversation);

  Optional<PersistingConversation> load(String id);

  List<ConversationSummary> list();

  /** Human-readable storage location for shell output. */
  String storageDescription();
}
