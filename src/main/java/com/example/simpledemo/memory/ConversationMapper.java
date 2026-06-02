package com.example.simpledemo.memory;

import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Conversation;
import com.embabel.chat.Message;
import com.embabel.chat.MessageRole;
import com.embabel.chat.SystemMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Maps between Embabel {@link Message}s and stored records. */
final class ConversationMapper {

  private ConversationMapper() {}

  static ConversationMemoryState memoryState(Conversation conversation) {
    if (conversation instanceof ConversationMemoryAccessor accessor) {
      return accessor.memoryState();
    }
    return ConversationMemoryState.empty();
  }

  static String previewFromMessages(List<StoredMessageRecord> messages, String title) {
    return messages.stream()
        .filter(message -> MessageRole.USER.name().equals(message.role()))
        .reduce((first, second) -> second)
        .map(StoredMessageRecord::content)
        .map(ConversationMapper::truncate)
        .orElse(title != null ? title : "(no messages)");
  }

  static String truncate(String text) {
    var singleLine = text.replace('\n', ' ').trim();
    if (singleLine.length() <= 60) {
      return singleLine;
    }
    return singleLine.substring(0, 57) + "...";
  }

  static ConversationRecord toRecord(Conversation conversation, ConversationMemoryState memory) {
    var messages = conversation.getMessages().stream().map(ConversationMapper::toStored).toList();
    var title =
        messages.stream()
            .filter(message -> MessageRole.USER.name().equals(message.role()))
            .findFirst()
            .map(StoredMessageRecord::content)
            .map(ConversationMapper::truncate)
            .orElse("New conversation");
    var updatedAt = messages.isEmpty() ? Instant.now() : messages.getLast().timestamp();
    return new ConversationRecord(
        conversation.getId(),
        title,
        updatedAt,
        messages,
        memory.sessionSummary(),
        memory.summarizedThroughIndex());
  }

  static StoredMessageRecord toStored(Message message) {
    return new StoredMessageRecord(message.getRole().name(), message.getContent(), message.getTimestamp());
  }

  static PersistingConversation fromRecord(ConversationRecord record, ConversationStore store) {
    var messages = new ArrayList<Message>();
    for (var stored : record.messages()) {
      messages.add(fromStored(stored));
    }
    var conversation = new InMemoryConversation(messages, record.id(), true);
    var summarizedThrough = record.summarizedThroughIndex();
    if (record.sessionSummary() == null && summarizedThrough <= 0) {
      summarizedThrough = ConversationMemoryState.NONE_SUMMARIZED;
    }
    var memory = new ConversationMemoryState(record.sessionSummary(), summarizedThrough);
    return new PersistingConversation(conversation, store, memory);
  }

  static Message fromStored(StoredMessageRecord stored) {
    return switch (MessageRole.valueOf(stored.role())) {
      case USER -> new UserMessage(stored.content(), null, stored.timestamp());
      case ASSISTANT ->
          new AssistantMessage(stored.content(), null, null, List.of(), stored.timestamp());
      case SYSTEM -> new SystemMessage(stored.content(), stored.timestamp());
    };
  }
}
