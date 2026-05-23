# Tier 4 — RAG, embeddings, and MCP

Tier 4 extends the commit assistant with **retrieval** (repo docs, past commits) and **MCP** (external tools + IDE integration). Tier 1–3 (shell, memory, tools, router) live on other `feat/*` branches; Tier 4 is split into three git branches you can check out in order.

## Branches

| Branch | Tutorials | What you get |
|--------|-----------|--------------|
| `feat/tier4-rag` | 17–19 | Lucene index, one-shot RAG in commits, agentic `ToolishRag`, Ollama embeddings |
| `feat/tier4-vector-memory` | 20 | `SimpleVectorStore` remembers past commit suggestions |
| `feat/tier4-mcp` | 21–22 | Consume filesystem MCP; publish agents as MCP server |

```bash
git checkout feat/tier4-rag          # start here
# later: feat/tier4-vector-memory, feat/tier4-mcp
```

## Prerequisites

| Requirement | Used for |
|-------------|----------|
| Java 21, `./mvnw` | All tutorials |
| Ollama `gemma4:e4b` (chat LLM) | Agents + chat |
| Ollama `nomic-embed-text` | Lucene vector search + vector memory |
| Git | Commit agent |
| `rag-index` once per machine | Tutorials 17–18 |
| Node `npx` (optional) | Tutorial 21 (`mcp` profile, filesystem MCP) |
| Second terminal / Cursor | Tutorial 22 (`mcp-server` profile) |

```bash
ollama pull gemma4:e4b
ollama pull nomic-embed-text
```

## Tutorial 17 — Local RAG (Lucene)

**Embabel guide:** RAG, Lucene module.

1. Start the app: `./mvnw spring-boot:run`
2. Build the index:

```text
shell:> rag-index
```

3. Debug search without LLM:

```text
shell:> rag-search -q "conventional commits subject"
```

4. Generate a commit (RAG injects chunks into the prompt):

```text
shell:> x "generate a commit message for my current changes"
```

Sources are configured in `simple-demo.rag.sources` (defaults: `docs/COMMIT_CONVENTIONS.md`, `TUTORIAL.md`, `rag-sources/past-commits.sample.txt`).

## Tutorial 18 — Agentic RAG

**Embabel guide:** Agentic RAG, `ToolishRag`.

The LLM chooses when to call vector/text search tools:

```text
shell:> x "how do we format commits in this repo?"
```

Or in chat:

```text
shell:> chat
chat:> how do we format commits here?
```

(`ChatRouter` routes convention questions to `CommitStyleAgent`.)

## Tutorial 19 — Embeddings (Ollama)

**Embabel guide:** ONNX embeddings (optional); this demo uses Ollama only.

```properties
embabel.models.default-embedding-model=nomic-embed-text
spring.ai.ollama.embedding.options.model=nomic-embed-text
```

Lucene `vectorSearch` uses `ModelProvider.getEmbeddingService()`. If `rag-index` fails with “embedding service not found”, pull the model and restart.

## Tutorial 20 — Long-term vector memory

**Spring AI:** `VectorStoreChatMemoryAdvisor` (see `SpringAiVectorMemoryConfiguration` Javadoc for a parallel pattern).

Each generated commit is stored in `~/.simple-demo/vector-memory.json`. The next run recalls similar subjects:

```properties
simple-demo.vector-memory.enabled=true
```

Disable in tests with `simple-demo.vector-memory.enabled=false`.

| Concern | Tier 2 file chat (other branch) | Tutorial 20 |
|---------|----------------------------------|-------------|
| Full transcript | JSON per session | Not stored here |
| “Similar commit before?” | Keyword-poor | Vector similarity |

## Tutorial 21 — Consume MCP tools

**Embabel guide:** MCP consuming.

```bash
SPRING_PROFILES_ACTIVE=mcp ./mvnw spring-boot:run
```

Requires `npx` and `@modelcontextprotocol/server-filesystem`. Optional GitHub MCP is documented in `application-mcp.properties`.

```text
shell:> x "read docs/COMMIT_CONVENTIONS.md and summarize commit rules"
```

(`McpFilesystemAgent` uses tool group `filesystem`.)

## Tutorial 22 — Publish as MCP server

**Embabel guide:** MCP server starter.

```bash
SPRING_PROFILES_ACTIVE=mcp-server ./mvnw spring-boot:run
```

- SSE endpoint: `http://localhost:8081/sse`
- Goals with `@Export(remote = true)` become MCP tools (`CommitMessageAgent`, `CommitStyleAgent`)

**Cursor** (`~/.cursor/mcp.json` example):

```json
{
  "mcpServers": {
    "simple-demo-commit": {
      "url": "http://localhost:8081/sse"
    }
  }
}
```

Default `./mvnw spring-boot:run` (no profile) keeps the interactive shell only; MCP server autoconfig stays off via `spring.autoconfigure.exclude` until `simple-demo.mcp-server.enabled=true`.

## Mapping to Embabel / Spring AI docs

| Tutorial | Embabel | Spring AI |
|----------|---------|-----------|
| 17 | RAG, Lucene | — |
| 18 | Agentic RAG, `ToolishRag` | — |
| 19 | Embeddings / Ollama | Ollama embedding model |
| 20 | — | `SimpleVectorStore`, advisor pattern in Javadoc |
| 21 | MCP consuming, tool groups | MCP client |
| 22 | MCP server starter | `spring.ai.mcp.server` |

## Tests

```bash
./mvnw test
```

RAG and vector memory are disabled in `src/test/resources/application.properties` so CI does not need Ollama embeddings. `McpServerProfileTest` smoke-loads the `mcp-server` profile with a random port.
