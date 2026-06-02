package com.example.simpledemo.audit;

import java.util.List;

public interface AuditRepository {

  void insert(AuditEvent event);

  List<AuditEvent> findByConversationId(String conversationId, int limit);
}
