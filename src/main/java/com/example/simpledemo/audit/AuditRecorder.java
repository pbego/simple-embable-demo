package com.example.simpledemo.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuditRecorder {

  private final AuditRepository auditRepository;

  public AuditRecorder(AuditRepository auditRepository) {
    this.auditRepository = auditRepository;
  }

  public void record(String conversationId, String eventType, Map<String, Object> payload) {
    auditRepository.insert(new AuditEvent(conversationId, eventType, payload));
  }

  public void routerDecision(String conversationId, Object targets, String rationale, boolean explicit) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("targets", targets);
    payload.put("rationale", rationale);
    payload.put("explicitPrefix", explicit);
    record(conversationId, AuditEventTypes.ROUTER_DECISION, payload);
  }

  public void agentInvoked(String conversationId, String agent, String question) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("agent", agent);
    payload.put("questionPreview", truncate(question));
    record(conversationId, AuditEventTypes.AGENT_INVOKED, payload);
  }

  public void summaryUpdated(String conversationId, int summarizedThroughIndex, String summaryPreview) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("summarizedThroughIndex", summarizedThroughIndex);
    payload.put("summaryPreview", truncate(summaryPreview));
    record(conversationId, AuditEventTypes.SUMMARY_UPDATED, payload);
  }

  public void guardrailBlocked(String conversationId, String guardrail, String message) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("guardrail", guardrail);
    payload.put("message", message);
    record(conversationId, AuditEventTypes.GUARDRAIL_BLOCKED, payload);
  }

  public void processLinked(String processId, String hint) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("processId", processId);
    payload.put("hintPreview", truncate(hint));
    record(null, AuditEventTypes.PROCESS_LINKED, payload);
  }

  private static String truncate(String text) {
    if (text == null) {
      return "";
    }
    var singleLine = text.replace('\n', ' ').trim();
    if (singleLine.length() <= 120) {
      return singleLine;
    }
    return singleLine.substring(0, 117) + "...";
  }
}
