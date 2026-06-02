package com.example.simpledemo.audit;

/** Well-known {@code audit_events.event_type} values. */
public final class AuditEventTypes {

  public static final String ROUTER_DECISION = "router.decision";
  public static final String AGENT_INVOKED = "agent.invoked";
  public static final String GUARDRAIL_BLOCKED = "guardrail.blocked";
  public static final String SUMMARY_UPDATED = "summary.updated";
  public static final String PROCESS_LINKED = "process.linked";

  private AuditEventTypes() {}
}
