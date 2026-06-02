package com.example.simpledemo.memory;

import com.embabel.agent.api.identity.User;
import com.embabel.chat.Asset;
import com.embabel.chat.AssetTracker;
import com.embabel.chat.Conversation;
import com.embabel.chat.ConversationFormatter;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import com.embabel.common.ai.prompt.PromptContributor;
import java.util.List;

/**
 * {@link Conversation} decorator that persists to disk after each {@link #addMessage(Message)}.
 */
public class PersistingConversation implements Conversation, ConversationMemoryAccessor {

  private final Conversation delegate;
  private final ConversationStore store;
  private ConversationMemoryState memoryState;

  public PersistingConversation(Conversation delegate, ConversationStore store) {
    this(delegate, store, ConversationMemoryState.empty());
  }

  public PersistingConversation(
      Conversation delegate, ConversationStore store, ConversationMemoryState memoryState) {
    this.delegate = delegate;
    this.store = store;
    this.memoryState = memoryState != null ? memoryState : ConversationMemoryState.empty();
  }

  Conversation delegate() {
    return delegate;
  }

  @Override
  public ConversationMemoryState memoryState() {
    return memoryState;
  }

  @Override
  public void updateMemoryState(ConversationMemoryState state) {
    this.memoryState = state != null ? state : ConversationMemoryState.empty();
    store.save(this);
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public List<Message> getMessages() {
    return delegate.getMessages();
  }

  @Override
  public AssetTracker getAssetTracker() {
    return delegate.getAssetTracker();
  }

  @Override
  public List<Asset> getAssets() {
    return delegate.getAssets();
  }

  @Override
  public UserMessage lastMessageIfBeFromUser() {
    return delegate.lastMessageIfBeFromUser();
  }

  @Override
  public Message addMessage(Message message) {
    var added = delegate.addMessage(message);
    store.save(this);
    return added;
  }

  @Override
  public Message addMessageFrom(Message message, User author) {
    var added = delegate.addMessageFrom(message, author);
    store.save(this);
    return added;
  }

  @Override
  public Message addMessageFromTo(Message message, User from, User to) {
    var added = delegate.addMessageFromTo(message, from, to);
    store.save(this);
    return added;
  }

  @Override
  public Conversation last(int n) {
    return new PersistingConversation(delegate.last(n), store, memoryState);
  }

  @Override
  public PromptContributor promptContributor(ConversationFormatter conversationFormatter) {
    return delegate.promptContributor(conversationFormatter);
  }

  @Override
  public boolean persistent() {
    return true;
  }

  @Override
  public String infoString(Boolean verbose, int indent) {
    return delegate.infoString(verbose, indent);
  }
}
