# Tutorial 24 — REST + SSE process API

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## Start API mode

```bash
SPRING_PROFILES_ACTIVE=api ./mvnw spring-boot:run
```

Shell is disabled; HTTP listens on **8080**. Look for `Tomcat started on port 8080` in the log.

## Trigger a commit process

```bash
curl -s -X POST http://localhost:8080/api/demo/commit \
  -H 'Content-Type: application/json' \
  -d '{"hint":"focus on API changes"}'
```

Response includes `processId`, `statusUrl`, and `eventsUrl`.

Then poll `statusUrl` until `status` is `COMPLETED`. `result` holds the commit message.

```bash
curl -s http://localhost:8080/api/v1/process/<processId>
```

## Platform endpoints

These come from the `embabel-agent-starter-webmvc` dependency in `pom.xml`.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/process/{id}` | Process status and result |
| `GET /events/process/{id}` | SSE event stream |
| `DELETE /api/v1/process/{id}` | Terminate process |
| `GET /api/v1/platform-info` | Agents, goals and models on the platform |

Enabled via `application-api.properties`:

```properties
embabel.agent.platform.rest.process-status-enabled=true
embabel.agent.platform.rest.process-kill-enabled=true
embabel.agent.platform.rest.process-events-enabled=true
```

<details>
<summary>Why <code>application-api.properties</code> also sets <code>embabel.agent.shell.web-application-type</code></summary>

The Embabel shell starter turns the web server off before profile files are read. Without this line, port 8080 never opens and the app exits right after startup. [ShellWebModeEnvironmentPostProcessor.java](src/main/java/com/example/simpledemo/config/ShellWebModeEnvironmentPostProcessor.java) applies the profile's value. [TUTORIAL-MCP-SIMPLE.md](TUTORIAL-MCP-SIMPLE.md) explains it in more detail.

</details>

## Code

- [`CommitProcessController.java`](src/main/java/com/example/simpledemo/api/CommitProcessController.java) — demo trigger
- [`application-api.properties`](src/main/resources/application-api.properties) — profile
