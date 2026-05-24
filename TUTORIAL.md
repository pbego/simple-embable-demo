# Embabel simple-demo tutorial

Local [Embabel](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) + [Ollama](https://ollama.com/). No gRPC, no cloud API keys.

## What this project shows

### 1. Agent orchestrator — shell `x`

Embabel **Autonomy** selects the best `@Agent` for your text. The **planner** then runs the agent’s `@Action` chain.

Example: **CommitMessageAgent** (two steps, no manual workflow XML):

```
UserInput  →  collectChanges (git CLI)  →  GitChanges  →  generateCommitMessage (LLM)  →  CommitMessage
```

Also try:

- `x "hello"` → **GreetingAgent** (no LLM)
- `x "tell me a joke"` → **JokeAgent** (one LLM step)
- `x "what branch am I on?"` → **GitInfoAgent** (LLM + **git tools** — see below)

### Git tools (`@LlmTool`)

**GitRepository** exposes read-only git facts as LLM-callable tools (`currentBranch`, `shortStatus`, `lastCommit`, `listBranches`). **GitInfoAgent** passes the repository to the model with `ai.withToolObject(repository)` so it looks up real data instead of guessing.

```
UserInput  →  answerAboutRepo  →  LLM + GitRepository tools  →  plain-text answer
```

Run from a git work tree (or set `simple-demo.git.work-tree`). The tools execute local `git` commands via **GitExecutor**.

### 2. Chat + router — shell `chat`

**Chat** keeps conversation memory. Each message hits **ChatRouter**, which picks a worker in one of two ways:

1. **You prefix the agent** (skip routing LLM): `@greet`, `@joke`, `@commit` (aliases: `@git`, `@funny`, …)
2. **Natural language** — a small routing LLM reads the message and chooses greet / joke / commit (same pattern as IAX `Router` + `Subagent` tools)

| Example in `chat` | How it routes |
|-------------------|---------------|
| `@commit focus on API` | CommitMessageAgent (explicit) |
| `@joke about java` | JokeAgent (explicit) |
| `write a commit message for my changes` | Routing LLM → CommitMessageAgent |
| `tell me something funny` | Routing LLM → JokeAgent |

Commit work always runs `git` then the LLM (`CommitMessageAgent.answer`).

Configure via `DemoChatConfiguration`: `AgentProcessChatbot.utilityFromPlatform(...)`.

### 3. How this maps to iax-app-ai

| Embabel shell / concept | iax-app-ai Embabel daemon |
|-------------------------|---------------------------|
| `chat` | gRPC `chat` / `chatEx` + `Chatbot` + `ChatSession` |
| `x` | Each turn still runs agents; default `AgentType.SRE` uses LLM routing |
| `ChatRouter` | `Router.respond` |
| `@Agent` workers | `Support`, `Insights`, `Rca`, … |
| `x` multi-step | RCA workflows, `CommitMessageAgent`-style chains |

## Tier 4 on this branch (`feat/tier4-mcp`)

Full stack: [TUTORIAL-RAG.md](TUTORIAL-RAG.md), [TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md), **[TUTORIAL-MCP.md](TUTORIAL-MCP.md)** (MCP consume `mcp` profile, publish `mcp-server` profile). See [README.md](README.md#testing-this-branch-feattier4-mcp).

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| Java 21 | `pom.xml` |
| Maven | `./mvnw` |
| Ollama | `http://localhost:11434`, model `gemma4:e4b` |
| Embeddings | Tier 4: `nomic-embed-text` (exact tag from `ollama list`) |
| `npx` | MCP consume profile only |
| Git | Only for commit `x` examples |

## Configuration

| Property | Default | Purpose |
|----------|---------|---------|
| `spring.ai.ollama.base-url` | `http://localhost:11434` | Ollama |
| `embabel.models.default-llm` | `gemma4:e4b` | Default model |
| `simple-demo.git.work-tree` | `.` | Repo for `git -C` |

## Try it

```bash
./mvnw spring-boot:run
```

```text
shell:> agents

# Orchestrator
shell:> x "generate a commit message for my current changes"
shell:> x "tell me a joke about spring"
shell:> x "what branch am I on and what was the last commit?"

# Chat + router
shell:> chat
chat:> hello
chat:> tell me a joke
chat:> @commit message for my staged changes
chat:> exit
```

## Commit message agent (detail)

**GitChangesCollector** runs `git branch`, `git status --short`, `git diff --staged`, `git diff` (truncated at 12k chars each).

The LLM returns JSON `CommitMessage` (`subject`, `body`) using Conventional Commits guidance.

Run from a git work tree or set `simple-demo.git.work-tree=/path/to/repo`.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Model not found | `ollama list` — tag must match `embabel.models.default-llm` |
| Empty git diffs | Run from repo root or set `simple-demo.git.work-tree` |
| Wrong agent in chat | Use `@commit` / `@joke` / `@greet`, or rephrase for the routing LLM |

## Tests

```bash
./mvnw test
```

Includes `ChatRouterTest`, `GitRepositoryTest` (tool registration and git invocation), and `GitInfoAgentTest` (fake LLM with tools).

## Further reading

- [TUTORIAL-MCP.md](TUTORIAL-MCP.md) — MCP on `feat/tier4-mcp`
- [TUTORIAL-RAG.md](TUTORIAL-RAG.md) · [TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md)
- [README.md](README.md) — how to test the current branch
- [Embabel User Guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)
- [iax-app-ai docs/embabel-flow.md](https://github.com/ITRS-Group/iax-app-ai/blob/dev/docs/embabel-flow.md) (production routing)
