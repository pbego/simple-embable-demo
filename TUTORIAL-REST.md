# Tutorial 24 — REST + SSE process API

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## Start API mode

```bash
SPRING_PROFILES_ACTIVE=api ./mvnw spring-boot:run
```

Shell is disabled; HTTP listens on **8080**.

## Trigger a commit process

```bash
curl -s -X POST http://localhost:8080/api/demo/commit \
  -H 'Content-Type: application/json' \
  -d '{"hint":"focus on API changes"}'
```

Response includes `processId`, `statusUrl`, and `eventsUrl`.

## Platform endpoints (Embabel 0.4.0)

| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/process/{id}` | Process status and result |
| `GET /events/process/{id}` | SSE event stream |
| `DELETE /api/v1/process/{id}` | Terminate process |

Enabled via `application-api.properties`:

```properties
embabel.agent.platform.rest.process-status-enabled=true
embabel.agent.platform.rest.process-events-enabled=true
```

## Code

- [`CommitProcessController.java`](src/main/java/com/example/simpledemo/api/CommitProcessController.java) — demo trigger
- [`application-api.properties`](src/main/resources/application-api.properties) — profile
