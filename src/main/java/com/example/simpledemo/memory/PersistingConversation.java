package com.example.simpledemo.memory;

import com.embabel.chat.Asset;
import com.embabel.chat.AssetTracker;
import com.embabel.chat.Conversation;
import com.embabel.chat.ConversationFormatter;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import com.embabel.agent.api.identity.User;
import com.embabel.common.ai.prompt.PromptContributor;
import java.util.List;

/**
 * {@link Conversation} decorator that persists to disk after each {@link #addMessage(Message)}.
 */
public class PersistingConversation implements Conversation {

  private final Conversation delegate;
  private final FileConversationStore store;

  public PersistingConversation(Conversation delegate, FileConversationStore store) {
    this.delegate = delegate;
    this.store = store;
  }

  Conversation delegate() {
    return delegate;
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
    return new PersistingConversation(delegate.last(n), store);
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
