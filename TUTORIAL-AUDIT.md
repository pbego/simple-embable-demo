# Audit and chat storage

This guide explains how **simple-demo** separates **chat transcripts** from **audit events**, and how to run storage on **PostgreSQL** (Docker) or **JSON files** (no database).

See also [TUTORIAL-MEMORY.md](TUTORIAL-MEMORY.md) for summarization and [TUTORIAL-MEMORY-SUMMARIZATION.md](TUTORIAL-MEMORY-SUMMARIZATION.md) for the rolling summary.

## Two layers

| Layer | What it stores | Postgres | File profile |
| ----- | ---------------- | -------- | -------------- |
| **Transcript** | Every user/assistant message | `messages` + `conversations` | `~/.simple-demo/conversations/*.json` |
| **Audit** | Routing, agents, guardrails, summaries, API process ids | `audit_events` (JSONB) | Not persisted (no-op) |

Audit events are **append-only** in application code: the demo inserts rows but does not update or delete them.

## Start PostgreSQL

```bash
docker compose up -d
# wait until healthy
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Local credentials (tutorial only): database `simple_demo`, user/password `simple_demo`, port `5432`.

## Shell commands

```text
shell:> chat
shell:> conversations
shell:> resume-chat <id>
shell:> audit-tail <id>
shell:> audit-tail <id> --limit 20
```

`audit-tail` is available only with the **postgres** profile.

## Event types

| `event_type` | When |
| ------------ | ---- |
| `router.decision` | Chat router chose targets (LLM or `@commit` prefix) |
| `agent.invoked` | A specialist agent ran (`COMMIT`, `JOKE`, …) |
| `guardrail.blocked` | `CommitSafetyGuardRail` rejected input |
| `summary.updated` | Rolling `session_summary` was refreshed |
| `process.linked` | REST `POST /api/demo/commit` returned a process id |

## Example SQL

```sql
SELECT event_type, payload, created_at
FROM audit_events
WHERE conversation_id = 'a1b2c3d4'
ORDER BY created_at;
```

## File profile (no Docker)

```bash
./mvnw spring-boot:run
# or explicitly:
./mvnw spring-boot:run -Dspring-boot.run.profiles=file
```

Transcripts are JSON under `simple-demo.conversations-dir` (default `~/.simple-demo/conversations`). Audit calls are accepted but not written to disk.

## Production note

Embabel applications may use **`embabel-chat-store`** (Neo4j) with the same `ConversationFactory` contract. This demo uses **PostgreSQL** to teach JDBC, Flyway, and SQL audit queries without extra graph infrastructure.
