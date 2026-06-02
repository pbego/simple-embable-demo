package com.example.simpledemo.memory;

import com.embabel.chat.Conversation;
import com.embabel.chat.ConversationFactory;
import com.embabel.chat.ConversationStoreType;
import com.embabel.chat.support.InMemoryConversation;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("postgres")
public class PostgresConversationFactory implements ConversationFactory {

  private final PostgresConversationStore store;

  public PostgresConversationFactory(PostgresConversationStore store) {
    this.store = store;
  }

  @Override
  public ConversationStoreType getStoreType() {
    return ConversationStoreType.IN_MEMORY;
  }

  @Override
  public Conversation create(String id) {
    return store
        .load(id)
        .orElseGet(
            () ->
                new PersistingConversation(
                    new InMemoryConversation(java.util.List.of(), id, true), store));
  }

  @Override
  public Conversation load(String id) {
    return store.load(id).orElse(null);
  }

  public Conversation createNew() {
    return create(UUID.randomUUID().toString().substring(0, 8));
  }
}
