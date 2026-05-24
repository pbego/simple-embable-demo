# simple-demo

Minimal Embabel examples: **orchestrator** (`x`) vs **chat** (`chat` + router), with local Ollama.

The **`main`** branch is the baseline. Each **`feat/*`** branch adds one topic; the hands-on guide for that topic is a **`TUTORIAL-*.md` file on that branch** (not on `main`).

## Testing this branch (feat/tier4-vector-memory)

This branch **includes everything on `feat/tier4-rag`** (Lucene RAG over repo docs) **plus semantic memory** of **your past commit suggestions** via Spring AI `SimpleVectorStore`.

| Topic | Tutorial |
|-------|----------|
| Lucene RAG (docs, `rag-index`) | [TUTORIAL-RAG.md](TUTORIAL-RAG.md) |
| Vector memory (past commits) | **[TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md)** ← focus here |

### Prerequisites

| Requirement | Command / notes |
|-------------|-----------------|
| Java 21, Maven | `./mvnw` |
| Ollama | `http://localhost:11434` |
| Chat LLM | `ollama pull gemma4:e4b` |
| **Embedding model** | `ollama pull nomic-embed-text` — used for **both** Lucene RAG and vector memory |
| Git | Commit-message examples |

```bash
ollama list | grep nomic-embed-text
```

Startup should report at least one embedding model. Vector memory also needs Spring AI’s `EmbeddingModel` bean (`spring.ai.ollama.embedding.options.model=nomic-embed-text` in `application.properties`).

### Quick test run

```bash
git checkout feat/tier4-vector-memory   # if needed
./mvnw spring-boot:run
```

```text
embabel> help
embabel> rag-index
embabel> x "generate a commit message for my current changes"
# make a small change (or run again with similar diff)
embabel> x "generate a commit message for my current changes"
```

| Step | What you are checking |
|------|------------------------|
| `rag-index` | Lucene index for **static** repo docs (see [TUTORIAL-RAG.md](TUTORIAL-RAG.md)) |
| First `x` commit | RAG style guide + empty vector memory → LLM suggests a commit → **`remember`** writes to `~/.simple-demo/vector-memory.json` |
| Second `x` commit | Prompt may include **“Similar past commits (vector memory)”** from the first suggestion |

There is **no** `vector-memory-index` shell command — recall and store happen inside `CommitMessageAgent` automatically.

### Disable vector memory only (optional)

```properties
simple-demo.vector-memory.enabled=false
```

RAG can stay on (`simple-demo.rag.enabled=true`). Tests disable both RAG and vector memory.

### Unit tests

```bash
./mvnw test
```

No Ollama required — see `src/test/resources/application.properties`.

### Troubleshooting

| Symptom | Fix |
|---------|-----|
| `nomic-embed-text' not found in available models: []` | `ollama pull nomic-embed-text`; set `embabel.models.default-embedding-model` to the **exact** tag from `ollama list` (often `nomic-embed-text:latest`) |
| `rag-index` not in `help` | Lucene bean failed — see [TUTORIAL-RAG.md](TUTORIAL-RAG.md) troubleshooting |
| Second commit never shows “Similar past commits” | Vector memory inactive (`enabled=false`) or first run had blank subject; check `~/.simple-demo/vector-memory.json` exists and grows |
| `Lock held by another program` on `lucene-index/write.lock` | Another JVM still running — stop it (`lsof ~/.simple-demo/lucene-index/write.lock`, then `kill <pid>`), or close extra terminals running `spring-boot:run` |

---

## Two ways to run agents (this branch)

| Shell command | On this branch |
|---------------|----------------|
| **`x "..."`** | `CommitMessageAgent`: git → **RAG** (docs) + **vector memory** (past suggestions) → LLM → **remember**; also `CommitStyleAgent`, `JokeAgent`, `GreetingAgent` |
| **`chat`** | `ChatRouter` → sub-agents (commit path uses same `CommitMessageAgent` logic) |

## Feature branches

| Branch | Topic | Tutorial |
|--------|--------|----------|
| **`main`** | Baseline | [TUTORIAL.md](TUTORIAL.md) |
| **`feat/tier4-rag`** | Lucene RAG | [TUTORIAL-RAG.md](TUTORIAL-RAG.md) |
| **`feat/tier4-vector-memory`** | `SimpleVectorStore` for past commits | **[TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md)** ← you are here |
| **`feat/tier4-mcp`** | MCP on tier-4 stack | `TUTORIAL-MCP.md` |

Suggested tier-4 order: **`feat/tier4-rag`** → **`feat/tier4-vector-memory`** → **`feat/tier4-mcp`**.

## Layout (this branch)

```
src/main/java/com/example/simpledemo/
├── agent/
│   ├── CommitMessageAgent.java   # RAG + vector recall → LLM → remember
│   ├── CommitStyleAgent.java
│   └── …
├── memory/
│   └── CommitVectorMemory.java     # SimpleVectorStore remember / recallSimilar
├── rag/                            # Lucene (from tier4-rag)
├── config/
│   ├── RagConfiguration.java
│   ├── VectorMemoryConfiguration.java
│   └── VectorMemoryProperties.java
└── shell/
    └── RagShellCommands.java       # rag-index, rag-search
```

## Docs

- **[TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md)** — concepts, flows, storage (this branch)
- [TUTORIAL-RAG.md](TUTORIAL-RAG.md) — Lucene RAG (also on this branch)
- [TUTORIAL.md](TUTORIAL.md) — baseline orchestrator / chat
- [Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)
