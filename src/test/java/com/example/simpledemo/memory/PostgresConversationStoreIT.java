package com.example.simpledemo.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.chat.AssistantMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import com.example.simpledemo.audit.AuditEvent;
import com.example.simpledemo.audit.AuditEventTypes;
import com.example.simpledemo.audit.JdbcAuditRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    properties = {
      "simple-demo.rag.enabled=false",
      "simple-demo.vector-memory.enabled=false",
      "embabel.agent.shell.interactive.enabled=false",
      "spring.shell.interactive.enabled=false",
      "spring.shell.noninteractive.enabled=false",
      "spring.main.web-application-type=none"
    })
@ActiveProfiles("postgres")
class PostgresConversationStoreIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("simple_demo")
          .withUsername("simple_demo")
          .withPassword("simple_demo");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired PostgresConversationStore conversationStore;

  @Autowired JdbcAuditRepository auditRepository;

  @Test
  void saveLoadListAndAuditRoundTrip() {
    var conversation = new InMemoryConversation(java.util.List.of(), "pg-test", true);
    var persisting = new PersistingConversation(conversation, conversationStore);
    persisting.addMessage(new UserMessage("Hello postgres"));
    persisting.addMessage(new AssistantMessage("Stored in SQL"));

    auditRepository.insert(
        new AuditEvent("pg-test", AuditEventTypes.ROUTER_DECISION, Map.of("targets", "greet")));

    var loaded = conversationStore.load("pg-test");
    assertTrue(loaded.isPresent());
    assertEquals(2, loaded.get().getMessages().size());

    var summaries = conversationStore.list();
    assertTrue(summaries.stream().anyMatch(summary -> "pg-test".equals(summary.id())));

    var audit = auditRepository.findByConversationId("pg-test", 10);
    assertEquals(1, audit.size());
    assertEquals(AuditEventTypes.ROUTER_DECISION, audit.getFirst().eventType());
  }
}
