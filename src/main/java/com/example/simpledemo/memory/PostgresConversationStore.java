package com.example.simpledemo.memory;

import com.embabel.chat.Conversation;
import com.embabel.chat.support.InMemoryConversation;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("postgres")
public class PostgresConversationStore implements ConversationStore {

  private final JdbcClient jdbc;

  public PostgresConversationStore(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public String storageDescription() {
    return "PostgreSQL (conversations + messages)";
  }

  @Override
  @Transactional
  public void save(Conversation conversation) {
    var memory = ConversationMapper.memoryState(conversation);
    var record = ConversationMapper.toRecord(conversation, memory);
    var now = Timestamp.from(Instant.now());

    jdbc
        .sql(
            """
            INSERT INTO conversations (
                id, title, session_summary, summarized_through_index, updated_at, created_at
            ) VALUES (
                :id, :title, :summary, :summarizedThrough, :updatedAt, :createdAt
            )
            ON CONFLICT (id) DO UPDATE SET
                title = EXCLUDED.title,
                session_summary = EXCLUDED.session_summary,
                summarized_through_index = EXCLUDED.summarized_through_index,
                updated_at = EXCLUDED.updated_at
            """)
        .param("id", record.id())
        .param("title", record.title())
        .param("summary", record.sessionSummary())
        .param("summarizedThrough", record.summarizedThroughIndex())
        .param("updatedAt", Timestamp.from(record.updatedAt()))
        .param("createdAt", now)
        .update();

    var existingCount =
        jdbc
            .sql("SELECT COUNT(*) FROM messages WHERE conversation_id = :id")
            .param("id", record.id())
            .query(Integer.class)
            .single();

    var messages = record.messages();
    for (var index = existingCount; index < messages.size(); index++) {
      var stored = messages.get(index);
      jdbc
          .sql(
              """
              INSERT INTO messages (conversation_id, seq, role, content, created_at)
              VALUES (:conversationId, :seq, :role, :content, :createdAt)
              """)
          .param("conversationId", record.id())
          .param("seq", index)
          .param("role", stored.role())
          .param("content", stored.content())
          .param("createdAt", Timestamp.from(stored.timestamp()))
          .update();
    }
  }

  @Override
  public Optional<PersistingConversation> load(String id) {
    var conversationRow =
        jdbc
            .sql(
                """
                SELECT id, title, session_summary, summarized_through_index, updated_at
                FROM conversations WHERE id = :id
                """)
            .param("id", id)
            .query(
                (rs, rowNum) ->
                    new LoadedConversation(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("session_summary"),
                        rs.getInt("summarized_through_index"),
                        rs.getTimestamp("updated_at").toInstant()))
            .optional();

    if (conversationRow.isEmpty()) {
      return Optional.empty();
    }

    var loaded = conversationRow.get();
    var messages =
        jdbc
            .sql(
                """
                SELECT role, content, created_at
                FROM messages
                WHERE conversation_id = :id
                ORDER BY seq ASC
                """)
            .param("id", id)
            .query(
                (rs, rowNum) ->
                    new StoredMessageRecord(
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toInstant()))
            .list();

    var record =
        new ConversationRecord(
            loaded.id(),
            loaded.title(),
            loaded.updatedAt(),
            messages,
            loaded.sessionSummary(),
            loaded.summarizedThroughIndex());

    return Optional.of(ConversationMapper.fromRecord(record, this));
  }

  @Override
  public List<ConversationSummary> list() {
    return jdbc
        .sql(
            """
            SELECT c.id, c.updated_at, c.title,
                   (SELECT m.content FROM messages m
                    WHERE m.conversation_id = c.id AND m.role = 'USER'
                    ORDER BY m.seq DESC LIMIT 1) AS last_user_content
            FROM conversations c
            ORDER BY c.updated_at DESC
            """)
        .query(
            (rs, rowNum) -> {
              var preview =
                  rs.getString("last_user_content") != null
                      ? ConversationMapper.truncate(rs.getString("last_user_content"))
                      : ConversationMapper.truncate(rs.getString("title"));
              return new ConversationSummary(
                  rs.getString("id"), rs.getTimestamp("updated_at").toInstant(), preview);
            })
        .list();
  }

  public Conversation createEmpty(String id) {
    return new PersistingConversation(new InMemoryConversation(List.of(), id, true), this);
  }

  private record LoadedConversation(
      String id, String title, String sessionSummary, int summarizedThroughIndex, Instant updatedAt) {}
}
