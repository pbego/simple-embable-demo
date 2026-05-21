package com.example.simpledemo.memory;

import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Conversation;
import com.embabel.chat.Message;
import com.embabel.chat.MessageRole;
import com.embabel.chat.SystemMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Persists {@link Conversation} instances as JSON files under a configurable directory.
 */
@Component
public class FileConversationStore {

  private final Path conversationsDir;
  private final ObjectMapper objectMapper;

  public FileConversationStore(ConversationMemoryProperties properties) {
    this.conversationsDir = Path.of(properties.conversationsDir()).toAbsolutePath().normalize();
    this.objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public Path conversationsDir() {
    return conversationsDir;
  }

  public void save(Conversation conversation) {
    try {
      Files.createDirectories(conversationsDir);
      var record = toRecord(conversation);
      var file = fileForId(conversation.getId());
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), record);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to save conversation " + conversation.getId(), e);
    }
  }

  public Optional<Conversation> load(String id) {
    var file = fileForId(id);
    if (!Files.isRegularFile(file)) {
      return Optional.empty();
    }
    try {
      var record = objectMapper.readValue(file.toFile(), ConversationRecord.class);
      return Optional.of(fromRecord(record));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load conversation " + id, e);
    }
  }

  public List<ConversationSummary> list() {
    if (!Files.isDirectory(conversationsDir)) {
      return List.of();
    }
    try (Stream<Path> paths = Files.list(conversationsDir)) {
      return paths
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .map(this::readSummary)
          .flatMap(Optional::stream)
          .sorted(Comparator.comparing(ConversationSummary::updatedAt).reversed())
          .toList();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to list conversations in " + conversationsDir, e);
    }
  }

  private Optional<ConversationSummary> readSummary(Path file) {
    try {
      var record = objectMapper.readValue(file.toFile(), ConversationRecord.class);
      return Optional.of(
          new ConversationSummary(record.id(), record.updatedAt(), preview(record)));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private static String preview(ConversationRecord record) {
    return record.messages().stream()
        .filter(message -> "USER".equals(message.role()))
        .reduce((first, second) -> second)
        .map(StoredMessageRecord::content)
        .map(FileConversationStore::truncate)
        .orElse(record.title() != null ? record.title() : "(no messages)");
  }

  private static String truncate(String text) {
    var singleLine = text.replace('\n', ' ').trim();
    if (singleLine.length() <= 60) {
      return singleLine;
    }
    return singleLine.substring(0, 57) + "...";
  }

  private ConversationRecord toRecord(Conversation conversation) {
    var messages =
        conversation.getMessages().stream().map(FileConversationStore::toStored).toList();
    var title =
        messages.stream()
            .filter(message -> MessageRole.USER.name().equals(message.role()))
            .findFirst()
            .map(StoredMessageRecord::content)
            .map(FileConversationStore::truncate)
            .orElse("New conversation");
    var updatedAt =
        messages.isEmpty()
            ? Instant.now()
            : messages.getLast().timestamp();
    return new ConversationRecord(conversation.getId(), title, updatedAt, messages);
  }

  private static StoredMessageRecord toStored(Message message) {
    return new StoredMessageRecord(message.getRole().name(), message.getContent(), message.getTimestamp());
  }

  private static Conversation fromRecord(ConversationRecord record) {
    var messages = new ArrayList<Message>();
    for (var stored : record.messages()) {
      messages.add(fromStored(stored));
    }
    return new InMemoryConversation(messages, record.id(), true);
  }

  private static Message fromStored(StoredMessageRecord stored) {
    return switch (MessageRole.valueOf(stored.role())) {
      case USER -> new UserMessage(stored.content(), null, stored.timestamp());
      case ASSISTANT -> new AssistantMessage(stored.content(), null, null, List.of(), stored.timestamp());
      case SYSTEM -> new SystemMessage(stored.content(), stored.timestamp());
    };
  }

  private Path fileForId(String id) {
    return conversationsDir.resolve(id + ".json");
  }
}
