package com.example.simpledemo.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcAuditRepository implements AuditRepository {

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

  private final JdbcClient jdbc;
  private final ObjectMapper objectMapper;

  public JdbcAuditRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  @Override
  public void insert(AuditEvent event) {
    try {
      var payloadJson = objectMapper.writeValueAsString(event.payload());
      jdbc
          .sql(
              """
              INSERT INTO audit_events (conversation_id, event_type, payload, created_at)
              VALUES (:conversationId, :eventType, CAST(:payload AS jsonb), :createdAt)
              """)
          .param("conversationId", event.conversationId())
          .param("eventType", event.eventType())
          .param("payload", payloadJson)
          .param("createdAt", Timestamp.from(event.createdAt()))
          .update();
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize audit payload", e);
    }
  }

  @Override
  public List<AuditEvent> findByConversationId(String conversationId, int limit) {
    return jdbc
        .sql(
            """
            SELECT conversation_id, event_type, payload, created_at
            FROM audit_events
            WHERE conversation_id = :conversationId
            ORDER BY created_at ASC
            LIMIT :limit
            """)
        .param("conversationId", conversationId)
        .param("limit", limit)
        .query(this::mapRow)
        .list();
  }

  private AuditEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
    var payload = readPayload(rs.getString("payload"));
    return new AuditEvent(
        rs.getString("conversation_id"),
        rs.getString("event_type"),
        payload,
        rs.getTimestamp("created_at").toInstant());
  }

  private Map<String, Object> readPayload(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(json, MAP_TYPE);
    } catch (JsonProcessingException e) {
      return new LinkedHashMap<>(Map.of("raw", json));
    }
  }
}
