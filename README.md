# simple-demo

Minimal Embabel examples on **`feat/all_together`**: orchestrator (`x`), chat with router, RAG, vector memory, MCP, REST, and multi-agent commit flows — all in one branch.

See **[TUTORIAL-INDEX.md](TUTORIAL-INDEX.md)** for the full topic list (tutorials 1–29). Guide coverage: **[Embabel](docs/GUIDE_COVERAGE.md)** · **[Spring AI](docs/SPRING_AI_GUIDE_COVERAGE.md)** · **[how they fit together](docs/EMBABEL_AND_SPRING_AI.md)**.

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| Java 21, Maven | `./mvnw` |
| [Ollama](https://ollama.com/) | `http://localhost:11434` |
| Chat LLM | `ollama pull gemma4:e4b` |
| Embeddings | `ollama pull nomic-embed-text` (for RAG + vector memory) |
| Git | Commit-message examples |
| `npx` | Only for `mcp` profile |

## Quick start (default profile — file-backed chat)

```bash
./mvnw spring-boot:run
```

### Chat history on PostgreSQL (Docker)

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Database: `simple_demo` on `localhost:5432` (user/password `simple_demo`). See [TUTORIAL-AUDIT.md](TUTORIAL-AUDIT.md).

```text
embabel> help
embabel> rag-index
embabel> x "generate a commit message for my current changes"
embabel> chat
chat:> @commit focus on router changes
chat:> @orchestrate full pipeline for my staged files
chat:> exit
embabel> conversations
embabel> resume-chat <id>
embabel> audit-tail <id>          # postgres profile only
embabel> commit-now "DOC-2: document REST API"
```

## Shell vs chat

| Command | Demonstrates |
|---------|----------------|
| **`x "..."`** | Autonomy picks an `@Agent`; planner runs `@Action` steps |
| **`chat`** | `ChatRouter` → specialists (`@commit`, `@style`, `@joke`, `@greet`, `@orchestrate`) |
| **`commit-now`** | `AgentInvocation` without chat (CI-friendly) |

## Profiles

| Profile | Command | Purpose |
|---------|---------|---------|
| *(default)* / `file` | `./mvnw spring-boot:run` | Shell + RAG + vector memory + JSON chat history |
| `postgres` | `docker compose up -d` then `SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run` | Same + PostgreSQL transcripts and audit |
| `mcp` | `SPRING_PROFILES_ACTIVE=mcp ./mvnw spring-boot:run` | Filesystem MCP tools |
| `mcp-server` | `SPRING_PROFILES_ACTIVE=mcp-server ./mvnw spring-boot:run` | SSE MCP server on :8081 |
| `api` | `SPRING_PROFILES_ACTIVE=api ./mvnw spring-boot:run` | REST `POST /api/demo/commit` + platform SSE |

## Tests

```bash
./mvnw test
```

No Ollama required for unit tests (RAG/vector memory disabled in test config).

## Layout

```
src/main/java/com/example/simpledemo/
├── agent/          # Commit, style, joke, git tools, orchestrator, security, changelog
├── api/            # REST (api profile)
├── chat/           # ChatRouter, summarization
├── config/         # RAG, vector memory, MCP, chat, guardrails
├── domain/         # Typed DICE-style records
├── git/            # GitExecutor, GitRepository
├── invocation/     # CommitInvocationRunner
├── memory/         # Conversation store (file or Postgres), vector memory
├── audit/          # Audit events (postgres profile)
├── rag/            # Lucene index
├── security/       # CommitSafetyGuardRail
└── shell/          # rag-index, conversations, commit-now
```

## Docs

- [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md) — all tutorials
- [docs/COMMIT_CONVENTIONS.md](docs/COMMIT_CONVENTIONS.md) — RAG corpus
- [Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)
