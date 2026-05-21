# simple-demo

Minimal Embabel examples: **orchestrator** (`x`) vs **chat** (`chat` + router), with local Ollama.

## Two ways to run agents

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

## Run

```bash
./mvnw spring-boot:run
```

### Orchestrator (`x`) — multi-step

```text
agents
x "generate a commit message for my current changes"
x "tell me a joke about kubernetes"
x "hello"
```

`CommitMessageAgent` runs **collectChanges** (git, no LLM) then **generateCommitMessage** (LLM).

### Chat — router + sub-agents

```text
chat
chat:> hello
chat:> tell me a joke about spring
chat:> @commit focus on the router changes
chat:> @joke kubernetes
chat:> exit
```

`ChatRouter` routes each turn in one of two ways:

- **Explicit agent prefix** — `@commit …`, `@joke …`, `@greet …` (you pick; no routing LLM)
- **Natural language** — routing LLM picks one or more agents (commit + joke in one message → both run)

Commit work runs `git` then the LLM via `CommitMessageAgent.answer(...)`.

## Layout

```
src/main/java/com/example/simpledemo/
├── agent/
│   ├── CommitMessageAgent.java   # multi-step @Agent (orchestrator)
│   ├── GreetingAgent.java        # no LLM
│   └── JokeAgent.java            # single-step LLM
├── chat/
│   └── ChatRouter.java           # @EmbabelComponent, trigger UserMessage
└── config/
    └── DemoChatConfiguration.java  # Chatbot bean for shell chat
```

## Docs

- [TUTORIAL.md](TUTORIAL.md) — commit message agent in detail
- [Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)

## Tests

```bash
./mvnw test
```

No Ollama required for unit tests.
