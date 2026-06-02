# Tutorial 23 — Unit and integration tests

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## What you learn

- Run `./mvnw test` without Ollama
- Mock the LLM for `CommitMessageAgent` (JUnit + Mockito)
- File conversation store and router tests from earlier topics

## Run tests

```bash
./mvnw test
```

`src/test/resources/application.properties` disables RAG, vector memory, and the shell.

## Key tests

| Test | Covers |
|------|--------|
| `CommitMessageAgentTest` | Jinja + mocked `PromptRunner.Rendering` |
| `ChatRouterTest` | `@commit` / `@style` prefixes, LLM routing helpers |
| `FileConversationStoreTest` | JSON persistence |
| `ChatContextServiceTest` | Summary + recent turns in enriched questions |
| `PostgresConversationStoreIT` | Postgres + Flyway (Testcontainers; skipped without Docker) |
| `CommitStyleRetrieverTest` | RAG (disabled in test props) |
| `CommitVectorMemoryTest` | Vector store |
| `GitRepositoryTest` / `GitExecutorReadOnlyTest` | Git tools + read-only mode |
| `McpServerProfileTest` / `ApiProfileTest` | Profile smoke loads |

## Optional Ollama smoke

Set `OLLAMA_SMOKE=1` and add an `@EnabledIfEnvironmentVariable` integration test if you want a live LLM check locally.
