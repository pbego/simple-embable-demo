# Embabel + Ollama Simple Demo — Tutorial

This project is a minimal [Embabel](https://docs.embabel.com/embabel-agent/guide/0.1.3/) agent that runs entirely on your machine using [Ollama](https://ollama.com/) and the **gemma4:e4b** model. No cloud API keys are required.

## What we built

- A **Spring Boot** application with two Embabel starters:
  - `embabel-agent-starter-ollama` — discovers models from your local Ollama server and registers them with Embabel
  - `embabel-agent-starter-shell` — interactive command-line interface to run agents
- A **TopicSummarizerAgent** with two typed actions:
  1. **extractTopic** — turns free-form shell input into a `Topic`
  2. **summarize** — writes a `Summary` (marked with `@AchievesGoal`)

Embabel’s planner (GOAP) infers the action chain from Java types: `UserInput` → `Topic` → `Summary`. You do not define an explicit workflow graph.

```
UserInput (shell)  →  extractTopic  →  Topic  →  summarize  →  Summary
```

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **Java 21** | Set in `pom.xml` |
| **Maven** | Use `./mvnw` in this project |
| **Ollama** | Running locally (default `http://localhost:11434`) |
| **gemma4:e4b** | Pulled in Ollama |

```bash
# Install/start Ollama, then pull the model
ollama pull gemma4:e4b
ollama list   # confirm gemma4:e4b appears
```

## Project layout

```
simple-demo/
├── pom.xml                          # Embabel BOM 0.4.0, ollama + shell starters
├── TUTORIAL.md                      # This file
└── src/main/
    ├── java/com/example/simpledemo/
    │   ├── SimpleDemoApplication.java
    │   └── agent/
    │       ├── Topic.java           # Domain type (step 1 output)
    │       ├── Summary.java         # Domain type (goal output)
    │       └── TopicSummarizerAgent.java
    └── resources/
        └── application.properties   # Ollama URL + default LLM
```

## How the agent works

### `@Agent`

Marks a Spring-managed class as an Embabel agent. The `description` helps the shell and planner choose this agent for matching intents.

### `@Action`

Each method is a step the planner can run. Parameters are **inputs from the blackboard** (previous step outputs or shell-provided types like `UserInput`).

### `@AchievesGoal`

Marks the action that completes the agent’s objective. When `summarize` finishes, the process is done.

### `Ai` and `withDefaultLlm()`

`Ai` is injected by Embabel. `withDefaultLlm()` uses `embabel.models.default-llm` from configuration (`gemma4:e4b`). `createObject(...)` asks the LLM to return structured JSON mapped to your Java `record`.

## Ollama connection

| Property | Value | Purpose |
|----------|--------|---------|
| `spring.ai.ollama.base-url` | `http://localhost:11434` | Ollama HTTP API |
| `embabel.models.default-llm` | `gemma4:e4b` | Model tag **exactly** as `ollama list` shows |

On startup, Embabel’s `OllamaModelsConfig` calls `GET /api/tags`, registers each model as an `Llm` bean, and wires Spring AI’s `OllamaChatModel`. If Ollama is down, startup may log a warning and fewer models will be available.

## Run and try

```bash
./mvnw spring-boot:run
```

When the shell prompt appears:

```text
agents
models
x "summarize the benefits of running LLMs locally with Ollama"
```

Useful shell commands:

| Command | Alias | Description |
|---------|-------|-------------|
| `execute "..."` | `x` | Run an agent for the given intent |
| `agents` | | List registered agents |
| `models` | | List available LLMs (should include `gemma4:e4b`) |
| `help` | | All commands |
| `exit` | `quit` | Leave the shell |

Optional flags on execute: `-p` log prompts, `-r` log LLM responses.

## Troubleshooting

| Problem | What to check |
|---------|----------------|
| **Default LLM not found** | Run `ollama list`. Set `embabel.models.default-llm` to the exact tag (including `:e4b`). |
| **Connection refused to Ollama** | Start Ollama; verify `curl http://localhost:11434/api/tags`. |
| **Slow or timeout** | Increase `embabel.agent.platform.http-client.read-timeout` (already set to `10m`). |
| **Empty or invalid JSON from model** | Smaller local models can struggle with structured output; retry or try a larger Ollama model. |
| **Maven dependency errors** | Ensure `spring-milestones` repo is in `pom.xml` (required for Embabel transitive deps). |

## Tests

```bash
./mvnw test
```

The included test only loads the Spring context (no live Ollama required in CI).

## Further reading

- [Embabel Agent User Guide](https://docs.embabel.com/embabel-agent/guide/0.1.3/)
- [Official Java/Kotlin examples](https://github.com/embabel/embabel-agent-examples)
- [Embabel Agent repository](https://github.com/embabel/embabel-agent)
