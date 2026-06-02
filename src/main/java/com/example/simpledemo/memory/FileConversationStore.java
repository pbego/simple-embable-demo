package com.example.simpledemo.memory;

import com.embabel.chat.Conversation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * Persists {@link Conversation} instances as JSON files under a configurable directory.
 */
@Repository
@Profile("file")
public class FileConversationStore implements ConversationStore {

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

  @Override
  public String storageDescription() {
    return conversationsDir.toString();
  }

  @Override
  public void save(Conversation conversation) {
    try {
      Files.createDirectories(conversationsDir);
      var memoryState = ConversationMapper.memoryState(conversation);
      var record = ConversationMapper.toRecord(conversation, memoryState);
      var file = fileForId(conversation.getId());
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), record);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to save conversation " + conversation.getId(), e);
    }
  }

  @Override
  public Optional<PersistingConversation> load(String id) {
    var file = fileForId(id);
    if (!Files.isRegularFile(file)) {
      return Optional.empty();
    }
    try {
      var record = objectMapper.readValue(file.toFile(), ConversationRecord.class);
      return Optional.of(ConversationMapper.fromRecord(record, this));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load conversation " + id, e);
    }
  }

  @Override
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
          new ConversationSummary(
              record.id(),
              record.updatedAt(),
              ConversationMapper.previewFromMessages(record.messages(), record.title())));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private Path fileForId(String id) {
    return conversationsDir.resolve(id + ".json");
  }
}
