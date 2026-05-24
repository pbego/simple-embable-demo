# Chat router

Richer routing in `ChatRouter`: keyword routes plus more explicit dispatch to specialist agents.

## Overview

On `main`, `ChatRouter` uses simple keywords (`joke`, `commit`, `git`). This branch improves **how user messages map to agents** — closer to production patterns where a router (or LLM) picks the right specialist.

You still use shell **`chat`**; each `UserMessage` triggers `@Action` on `ChatRouter`, which delegates to `GreetingAgent`, `JokeAgent`, `CommitMessageAgent`, or other handlers.

**Embabel guide:** Goals in chatbots, `@EmbabelComponent`, utility actions.

## Prerequisites

Same as `main` (Ollama `gemma4:e4b`, Java 21).

## Key code

| File | Role |
|------|------|
| `chat/ChatRouter.java` | Routing logic and `fromMessage` |
| `chat/ChatRouterTest.java` | Route unit tests |

## Try it

```bash
./mvnw spring-boot:run
```

```text
shell:> chat
chat:> hello
chat:> tell me a joke
chat:> generate a commit message for my changes
chat:> exit
```

Compare routing behavior with `main` by running the same phrases on both branches.

## Related branches

- `feat/memory` — persistent chat history
- `feat/tools` — git `@Tool` agent instead of keyword commit path

Branch index on `main`: [README.md](https://github.com/pbego/simple-embable-demo/blob/main/README.md#feature-branches).
