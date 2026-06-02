package com.example.simpledemo.audit;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class NoOpAuditRepository implements AuditRepository {

  @Override
  public void insert(AuditEvent event) {
    // File profile has no audit table; transcript remains in conversation JSON.
  }

  @Override
  public List<AuditEvent> findByConversationId(String conversationId, int limit) {
    return List.of();
  }
}
