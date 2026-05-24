# simple-demo

Minimal Embabel examples: **orchestrator** (`x`) vs **chat** (`chat` + router), with local Ollama.

The **`main`** branch is the baseline (commit agent, greeting, joke, keyword router). Each **`feat/*`** branch adds one topic; the hands-on guide for that topic is a **`TUTORIAL-*.md` file on that branch only** (not on `main`).

## Testing this branch (feat/tier4-rag)

You are on a branch that adds **Lucene RAG** (retrieval-augmented generation) over project docs, with **Ollama embeddings** for semantic search. Read **[TUTORIAL-RAG.md](TUTORIAL-RAG.md)** for what RAG is, how the flows work, and deeper detail.

### Prerequisites (this branch)

| Requirement | Command / notes |
|-------------|-----------------|
| Java 21, Maven | `./mvnw` |
| Ollama | Running at `http://localhost:11434` |
| Chat LLM | `ollama pull gemma4:e4b` — must match `embabel.models.default-llm` |
| **Embedding model** | `ollama pull nomic-embed-text` — **required**; startup fails if Ollama reports zero embedding models |
| Git | For commit-message examples |

Verify embeddings before starting:

```bash
ollama list | grep nomic-embed-text
```

Startup should log `Initialized … and 1 embedding(s)` and list `nomic-embed-text:latest` under available embedding services.

### Quick test run

```bash
git checkout feat/tier4-rag   # if needed
./mvnw spring-boot:run
```

In the Embabel shell (`embabel>` prompt):

```text
embabel> help
embabel> rag-index
embabel> rag-search --query "conventional commits subject"
embabel> x "generate a commit message for my current changes"
embabel> x "how do we format commits in this repo?"
embabel> chat
chat:> how do we format commits here?
```

If `rag-index` / `rag-search` are missing from `help`, the Lucene bean did not start (check startup logs for embedding model or index lock errors).

| Step | What you are checking |
|------|------------------------|
| `rag-index` | Ingests `docs/COMMIT_CONVENTIONS.md`, `TUTORIAL.md`, `rag-sources/past-commits.sample.txt` into `~/.simple-demo/lucene-index` |
| `rag-search` | Vector search only (no LLM) — confirms index + embeddings |
| `x` commit message | **One-shot RAG**: `CommitStyleRetriever` injects top chunks into the commit prompt |
| `x` / `chat` style question | **Agentic RAG**: `CommitStyleAgent` + `ToolishRag` lets the LLM call search tools |

### Run without RAG (optional)

If you only want the baseline agents and no Lucene index:

```properties
simple-demo.rag.enabled=false
```

(Tests already set this in `src/test/resources/application.properties`.)

### Unit tests

```bash
./mvnw test
```

No Ollama or embedding model required — RAG beans are disabled in test config.

### Troubleshooting

| Symptom | Fix |
|---------|-----|
| `Default embedding service 'nomic-embed-text:latest' not found … available models: []` | `ollama pull nomic-embed-text` |
| `RAG is disabled` / empty `rag-search` | `simple-demo.rag.enabled=true` and run `rag-index` |
| Lucene “no segments* file” on first start | Normal until `rag-index` builds the index |
| Chat answers about commits but ignores conventions | Run `rag-index` first; ask a **style** question (`how do we format commits`) not “generate commit message” |

---

## Two ways to run agents

| Shell command | What it demonstrates | On this branch |
|---------------|----------------------|----------------|
| **`x "..."`** | Autonomy picks a `@Agent`; planner runs `@Action` steps | `CommitMessageAgent` (git → RAG → LLM), `CommitStyleAgent` (agentic RAG), `JokeAgent`, `GreetingAgent` |
| **`chat`** | Persistent conversation; `ChatRouter` → sub-agent | Above agents + routes for commit vs style questions |

## Feature branches

| Branch | Topic | Tutorial on branch |
|--------|--------|-------------------|
| **`main`** | Baseline commit agent, `x` / `chat`, keyword router | [TUTORIAL.md](TUTORIAL.md) |
| **`feat/tier4-rag`** | Lucene RAG, one-shot + agentic retrieval, Ollama embeddings | **[TUTORIAL-RAG.md](TUTORIAL-RAG.md)** ← you are here |
| **`feat/tier4-vector-memory`** | Semantic memory of past commit suggestions | `TUTORIAL-VECTOR-MEMORY.md` |
| **`feat/tier4-mcp`** | MCP client/server on top of tier-4 stack | `TUTORIAL-MCP.md` |

Other `feat/*` branches (`jinja`, `memory`, `router`, `tools`, …) have their own `TUTORIAL-*.md` files when checked out.

Suggested order for tier 4: **`feat/tier4-rag`** → **`feat/tier4-vector-memory`** → **`feat/tier4-mcp`**.

## Layout (this branch)

```
src/main/java/com/example/simpledemo/
├── agent/
│   ├── CommitMessageAgent.java   # git → one-shot RAG → LLM
│   ├── CommitStyleAgent.java     # agentic RAG (ToolishRag)
│   ├── GreetingAgent.java
│   └── JokeAgent.java
├── chat/
│   └── ChatRouter.java           # routes STYLE vs COMMIT vs joke/greeting
├── config/
│   ├── RagConfiguration.java
│   └── RagProperties.java
├── rag/
│   ├── CommitCorpusIngester.java
│   └── CommitStyleRetriever.java
└── shell/
    └── RagShellCommands.java     # rag-index, rag-search
```

## Docs

- **[TUTORIAL-RAG.md](TUTORIAL-RAG.md)** — RAG concepts, flows, configuration, hands-on steps (this branch)
- [TUTORIAL.md](TUTORIAL.md) — baseline orchestrator / chat / commit agent
- [Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) — RAG, Lucene, `ToolishRag`
