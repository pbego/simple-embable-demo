# simple-demo

Minimal Embabel examples: **orchestrator** (`x`) vs **chat** (`chat` + router), with local Ollama.

The **`main`** branch is the baseline. Each **`feat/*`** branch adds one topic; the hands-on guide is a **`TUTORIAL-*.md` file on that branch**.

## Testing this branch (feat/tier4-mcp)

**Full tier‑4 stack:** Lucene RAG, vector memory of past commits, **MCP consume** (filesystem tools), and **MCP publish** (expose agents over SSE). This branch is the capstone — read **[TUTORIAL-MCP.md](TUTORIAL-MCP.md)** for MCP concepts and profiles.

| Topic | Tutorial |
|-------|----------|
| Lucene RAG | [TUTORIAL-RAG.md](TUTORIAL-RAG.md) |
| Vector memory | [TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md) |
| **MCP consume & publish** | **[TUTORIAL-MCP.md](TUTORIAL-MCP.md)** ← focus here |

### Prerequisites

| Requirement | Notes |
|-------------|--------|
| Java 21, Maven | `./mvnw` |
| Ollama | `http://localhost:11434` |
| Chat LLM | `ollama pull gemma4:e4b` |
| Embeddings | `ollama pull nomic-embed-text` — tag must match `ollama list` (often `nomic-embed-text:latest`) |
| Git | Commit-message examples |
| **`npx`** | Only for **`mcp` profile** (filesystem MCP server via stdio) |
| **Port 8081** | Only for **`mcp-server` profile** (SSE MCP server) |

### Mode 1 — Default shell (RAG + vector memory, no MCP)

```bash
./mvnw spring-boot:run
```

```text
embabel> help
embabel> rag-index
embabel> x "generate a commit message for my current changes"
```

### Mode 2 — Consume MCP tools (`mcp` profile)

Starts the app **and** a filesystem MCP server (via `npx`) so agents can read allowed paths under your home directory.

```bash
SPRING_PROFILES_ACTIVE=mcp ./mvnw spring-boot:run
```

```text
embabel> x "read docs/COMMIT_CONVENTIONS.md and summarize our commit rules"
```

Autonomy should pick **`McpFilesystemAgent`**, which calls the LLM with `withToolGroup("filesystem")`.

### Mode 3 — Publish agents as MCP server (`mcp-server` profile)

HTTP server on **8081**; Embabel shell is disabled. Remote clients (e.g. Cursor) call exported commit goals over SSE.

```bash
SPRING_PROFILES_ACTIVE=mcp-server ./mvnw spring-boot:run
```

| Setting | Value |
|---------|--------|
| SSE endpoint | `http://localhost:8081/sse` |
| Exported goals | `CommitMessageAgent.generateCommitMessage`, `CommitStyleAgent.explainCommitStyle` (`@Export(remote = true)`) |

Example Cursor entry in `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "simple-demo-commit": {
      "url": "http://localhost:8081/sse"
    }
  }
}
```

### Profiles summary

| Profile | Shell | RAG / memory | MCP client | MCP server |
|---------|-------|--------------|------------|------------|
| *(default)* | yes | yes | no | no |
| `mcp` | yes | yes | filesystem stdio | no |
| `mcp-server` | no | yes* | no | SSE on 8081 |

\*RAG/memory beans load if embeddings are available; disable via properties if needed.

### Unit tests

```bash
./mvnw test
```

Default tests disable RAG, vector memory, and MCP server. `McpServerProfileTest` checks the `mcp-server` profile loads.

### Troubleshooting

| Symptom | Fix |
|---------|-----|
| Embedding model not found | `ollama pull nomic-embed-text`; set `embabel.models.default-embedding-model` to exact `ollama list` tag |
| `Lock held` on `lucene-index/write.lock` | Only one JVM — `lsof ~/.simple-demo/lucene-index/write.lock` then stop extra `spring-boot:run` |
| `rag-index` missing from `help` | Lucene bean failed at startup — check embeddings / lock |
| MCP consume: no filesystem tools | Use `SPRING_PROFILES_ACTIVE=mcp`; ensure `npx` works; check logs for MCP client connection |
| MCP server: connection refused | Use `mcp-server` profile; confirm `http://localhost:8081/sse` |

---

## Two ways to run agents (default profile)

| Shell command | On this branch |
|---------------|----------------|
| **`x "..."`** | `CommitMessageAgent`, `CommitStyleAgent`, `McpFilesystemAgent` (`mcp` only), `JokeAgent`, `GreetingAgent` |
| **`chat`** | `ChatRouter` → sub-agents |

## Feature branches (tier 4)

| Branch | Adds |
|--------|------|
| **`feat/tier4-rag`** | Lucene RAG |
| **`feat/tier4-vector-memory`** | + `SimpleVectorStore` past commits |
| **`feat/tier4-mcp`** | + MCP client & server ← **you are here** |

## Layout

```
src/main/java/com/example/simpledemo/
├── agent/
│   ├── CommitMessageAgent.java      # @Export(remote) for MCP server
│   ├── CommitStyleAgent.java
│   ├── McpFilesystemAgent.java      # @Profile("mcp")
│   └── …
├── config/
│   ├── DemoMcpToolGroupsConfiguration.java
│   ├── McpServerEnableConfiguration.java
│   ├── RagConfiguration.java
│   └── VectorMemoryConfiguration.java
└── shell/RagShellCommands.java
```

## Docs

- **[TUTORIAL-MCP.md](TUTORIAL-MCP.md)** — MCP consume/publish (this branch)
- [TUTORIAL-RAG.md](TUTORIAL-RAG.md) · [TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md)
- [TUTORIAL.md](TUTORIAL.md) — baseline shell / chat
- [Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)
