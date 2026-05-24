# simple-demo

Minimal Embabel examples: **orchestrator** (`x`) vs **chat** (`chat` + router), with local Ollama.

The **`main`** branch is the baseline (commit agent, greeting, joke, keyword router). Each **`feat/*`** branch adds one topic; the hands-on guide for that topic is a **`TUTORIAL-*.md` file on that branch only** (not on `main`).

## Two ways to run agents (main)

| Shell command | What it demonstrates | In this project |
|---------------|----------------------|-----------------|
| **`x "..."`** | Agent orchestrator: Autonomy picks a `@Agent`, Embabel planner runs one or more `@Action` steps | `CommitMessageAgent` (git → LLM), `JokeAgent`, `GreetingAgent` |
| **`chat`** | Persistent conversation; each turn hits `ChatRouter` → sub-agent | `GreetingAgent`, `JokeAgent`, `CommitMessageAgent` (git + LLM) |

This mirrors the IAX Embabel daemon pattern: gRPC chat ≈ `chat`, multi-step agents ≈ `x` / planner, specialists ≈ `@Agent` workers, `ChatRouter` ≈ `Router`.

## Prerequisites

- Java 21, Maven (`./mvnw`)
- [Ollama](https://ollama.com/) at `http://localhost:11434`
- Model: `ollama pull gemma4:e4b` (must match `embabel.models.default-llm` in `application.properties`)
- Git (only for commit-message `x` examples)

## Run (main)

```bash
git checkout main
./mvnw spring-boot:run
```

```text
agents
x "generate a commit message for my current changes"
chat
```

See [TUTORIAL.md](TUTORIAL.md) on this branch for detail.

## Feature branches

Check out a branch, then read its tutorial file in the repo root.

| Branch | What we covered | Tutorial on branch |
|--------|-----------------|-------------------|
| **`main`** | Baseline: `CommitMessageAgent` (git → LLM), `GreetingAgent`, `JokeAgent`, shell `x` / `chat`, keyword `ChatRouter` | [TUTORIAL.md](TUTORIAL.md) |
| **`feat/jinja`** | Jinja/Jinjava prompts for commit generation; shared fragments under `prompts/commit/` | `TUTORIAL-JINJA.md` |
| **`feat/memory`** | File-backed chat history (`~/.simple-demo/conversations`), `resume-chat`, message windowing (last N turns to the model) | `TUTORIAL-MEMORY.md` |
| **`feat/memory-summarization`** | Everything on `feat/memory`, plus rolling **session summary** when the window is exceeded | `TUTORIAL-MEMORY-SUMMARIZATION.md` |
| **`feat/router`** | Richer `ChatRouter`: explicit and natural-language routing to specialists | `TUTORIAL-ROUTER.md` |
| **`feat/tools`** | `GitInfoAgent` with `@Tool` on `GitRepository` so the LLM queries real git state | `TUTORIAL-TOOLS.md` |
| **`feat/tier4-rag`** | Lucene RAG over repo docs, one-shot retrieval in commits, agentic `ToolishRag`, Ollama embeddings | `TUTORIAL-RAG.md` |
| **`feat/tier4-vector-memory`** | `SimpleVectorStore` remembers past commit suggestions (semantic recall) | `TUTORIAL-VECTOR-MEMORY.md` |
| **`feat/tier4-mcp`** | Full stack above + consume filesystem MCP + publish agents as MCP server (SSE) | `TUTORIAL-MCP.md` |

Suggested order: `main` → `feat/jinja` → `feat/memory` → `feat/memory-summarization` → `feat/router` → `feat/tools` → `feat/tier4-rag` → `feat/tier4-vector-memory` → `feat/tier4-mcp`.

```bash
git checkout feat/tier4-rag
cat TUTORIAL-RAG.md
./mvnw spring-boot:run
```

## Layout (main)

```
src/main/java/com/example/simpledemo/
├── agent/
│   ├── CommitMessageAgent.java
│   ├── GreetingAgent.java
│   └── JokeAgent.java
├── chat/
│   └── ChatRouter.java
└── config/
    └── DemoChatConfiguration.java
```

## Docs

- [TUTORIAL.md](TUTORIAL.md) — baseline commit agent and shell flows
- [Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)

## Tests

```bash
./mvnw test
```

No Ollama required for unit tests on `main`.
