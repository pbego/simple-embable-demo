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

### Chat routing

```text
chat
chat:> hello
chat:> @commit focus on the router changes
chat:> @style how do we format commits?
chat:> @joke kubernetes
```

`ChatRouter` uses explicit `@commit` / `@style` / `@joke` / `@greet` prefixes or an LLM router for natural language (including multi-agent replies in one turn).

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
