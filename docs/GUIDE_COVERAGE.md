# Embabel guide coverage — simple-demo vs official topics

This matrix maps [EMBABEL_AGENT_GUIDE_TOPICS.md](EMBABEL_AGENT_GUIDE_TOPICS.md) (summaries of the [official guide 0.5.0-SNAPSHOT](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)) to what **simple-demo** on **`feat/all_together`** actually demonstrates.

**Spring AI reference coverage:** [SPRING_AI_GUIDE_COVERAGE.md](SPRING_AI_GUIDE_COVERAGE.md) · **How Embabel uses Spring AI:** [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md)

| Status | Meaning |
|--------|---------|
| **Covered** | Runnable code and/or a dedicated tutorial |
| **Partial** | Touched indirectly, prose only, or subset of the topic |
| **Not covered** | Could be added to this repo |
| **Out of scope** | Wrong fit, version gap, or belongs upstream / elsewhere |

**Repo:** Embabel **0.4.0**, Java **21**, **Ollama-only** (no cloud API keys). The official guide targets **0.5.0-SNAPSHOT** — see [Version gap](#version-gap).

---

## Summary

| Status | ~Topics |
|--------|---------|
| Covered | 35 |
| Partial | 15 |
| Not covered | 25 |
| Out of scope | 8 |

---

## Chapter 1 — Overview

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 1. Overview | Partial | [TUTORIAL.md](../TUTORIAL.md) intro; no standalone “why JVM agents” chapter |
| 1.1 Glossary | Partial | Terms used in tutorials (Agent, Tools, MCP, DICE) |
| 1.2 Why an agent framework? | Partial | Links to official guide only |
| 1.3 Embabel differentiators | Partial | Implicit in design choices; not written up in-repo |
| 1.4 Core concepts | **Covered** | GOAP chains, typed flow — [TUTORIAL.md](../TUTORIAL.md), `CommitMessageAgent` |

---

## Chapter 2 — Getting Started

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 2.1 Quickstart | Partial | This repo is the demo; no template / `project-creator` walkthrough |
| 2.2 Getting the binaries | **Covered** | [pom.xml](../pom.xml): shell, Ollama, RAG, MCP server starters |
| 2.3 Getting Embabel running | **Covered** | [README.md](../README.md), profiles in [TUTORIAL-INDEX.md](../TUTORIAL-INDEX.md) |
| 2.4 Adding a little AI | Partial | `Ai` in `@Action` methods; no standalone `@Component` “injected AI only” example |
| 2.5 Writing your first agent | **Covered** | `GreetingAgent`, `JokeAgent`, `CommitMessageAgent` — [TUTORIAL.md](../TUTORIAL.md) |

---

## Chapter 3 — Embabel Shell

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 3.1 How to use the shell | **Covered** | [TUTORIAL.md](../TUTORIAL.md), [README.md](../README.md) |
| 3.2 Shell commands | **Covered** | `x`, `chat`, `commit-now`, `rag-index`, `conversations`, `resume-chat`; custom commands in `shell/` |
| 3.3 Embabel modules | Partial | Dependencies in `pom.xml`; no module catalog doc |

**Not demonstrated from guide:** `choose-goal`, `set-context` / `show-context`, `-p`/`-r` verbosity flags, open mode (`-o`) — easy additions to [TUTORIAL.md](../TUTORIAL.md).

---

## Chapter 4 — Reference

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 4.1 Invoking an agent | **Covered** | `x` (autonomy), `chat`, [TUTORIAL-INVOCATION.md](../TUTORIAL-INVOCATION.md) |
| 4.2 Agent process flow | **Covered** | Typed blackboard flow; REST/SSE — [TUTORIAL-REST.md](../TUTORIAL-REST.md) |
| 4.3 Goals, actions, conditions | **Covered** | `@Agent`, `@Action`, `@AchievesGoal` throughout `agent/` |
| 4.4 Domain objects | **Covered** | `GitRepository`, DICE records — [TUTORIAL-DICE.md](../TUTORIAL-DICE.md), [TUTORIAL-TOOLS.md](../TUTORIAL-TOOLS.md) |
| 4.5 Configuration | **Covered** | `application.properties`, profile-specific props |
| 4.6 Annotation model | Partial | `@Agent`, `@Action`, `@AchievesGoal`, `@EmbabelComponent`, `@LlmTool`, `canRerun`, `trigger`; not `@Condition`, `@Cost`, `@State`, subagents, etc. |
| 4.7 DSL | Not covered | Uses `@Action` chains instead of `SimpleAgentBuilder` / `ScatterGatherBuilder` |
| 4.8 Core types | **Covered** | `Ai`, `withDefaultLlm()`, `createObject()`, `.rendering()` — [TUTORIAL-JINJA.md](../TUTORIAL-JINJA.md) |
| 4.9 Tools | **Covered** | [TUTORIAL-TOOLS.md](../TUTORIAL-TOOLS.md), [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) |
| 4.10 Structured prompt elements | **Covered** | `PersistingConversation` → `PromptContributor` |
| 4.11 Templates | **Covered** | [TUTORIAL-JINJA.md](../TUTORIAL-JINJA.md) |
| 4.12 RAG | **Covered** | [TUTORIAL-RAG.md](../TUTORIAL-RAG.md) |
| 4.13 Building chatbots | **Covered** | `AgentProcessChatbot`, file memory — [TUTORIAL-MEMORY.md](../TUTORIAL-MEMORY.md), [TUTORIAL-ROUTER.md](../TUTORIAL-ROUTER.md) |
| 4.14 The AgentProcess | Partial | Process id via REST; not explained as a standalone concept |
| 4.15 Execution modes | Not covered | Default sequential only; no `CONCURRENT` profile demo |
| 4.16 ProcessOptions | Partial | Chat verbosity; no `toolCallContext`, budgets, ephemeral processes |
| 4.17 The AgentPlatform | Partial | Used via injection; no SPI customization |
| 4.18 Invoking Embabel agents | **Covered** | [TUTORIAL-INVOCATION.md](../TUTORIAL-INVOCATION.md), [TUTORIAL-REST.md](../TUTORIAL-REST.md) |
| 4.19 Using states | Not covered | No `@State` / looping states |
| 4.20 Choosing a planner | Partial | **GOAP** for `@Agent` pipelines; **Utility** for `chat` (`utilityFromPlatform`) — not Hybrid/Supervisor |
| 4.21 API vs SPI | Not covered | — |
| 4.22 Embabel and Spring | **Covered** | Entire Spring Boot application |
| 4.23 Working with LLMs | **Covered** (Ollama) | Local model + embeddings; single default LLM |
| 4.24 AWS Bedrock | Out of scope | Ollama-only repo |
| 4.25 MiniMax | Out of scope | Ollama-only repo |
| 4.26 Working with streams | Not covered | — |
| 4.27 LLM reasoning / thinking | Not covered | — |
| 4.28 Callbacks (interceptors) | Not covered | — |
| 4.29 Tracking LLM cost and usage | Not covered | — |
| 4.30 Working with guardrails | Partial | [TUTORIAL-SECURE-TOOLS.md](../TUTORIAL-SECURE-TOOLS.md); `UserInputGuardRail`, not full 0.5 `withGuardRails` / `@SecureAgentTool` |
| 4.31 Agent and action termination | Not covered | — |
| 4.32 Customizing Embabel | Out of scope | SPI / custom `LlmService` — framework extension |
| 4.33 Integrations | **Covered** | [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) |
| 4.34 Developer tooling | Partial | See 4.35 |
| 4.35 IntelliJ IDEA plugin | Out of scope | IDE install, not application code |
| 4.36 Agent Skills | Not covered | Needs Embabel **0.5+** |
| 4.37 Testing | **Covered** | [TUTORIAL-TESTING.md](../TUTORIAL-TESTING.md), [TUTORIAL-EVAL.md](../TUTORIAL-EVAL.md), `src/test/` |
| 4.38 Embabel architecture | Out of scope | Official diagrams |
| 4.39 Troubleshooting | Partial | Scattered tables in memory/MCP tutorials |
| 4.40 Migrating from other frameworks | Not covered | — |
| 4.41 API evolution | Partial | Pinned 0.4.0 vs guide 0.5.0 |

**Also covered (tutorial index, not a separate guide section):**

| Concept | Tutorial |
|---------|----------|
| Vector memory | [TUTORIAL-VECTOR-MEMORY.md](../TUTORIAL-VECTOR-MEMORY.md) |
| Session summarization | [TUTORIAL-MEMORY-SUMMARIZATION.md](../TUTORIAL-MEMORY-SUMMARIZATION.md) |
| Multi-agent orchestration (in-process) | [TUTORIAL-A2A.md](../TUTORIAL-A2A.md) |

---

## Chapter 5 — Asynchronous mode and Java 25

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 5. Async / Java 25 | Out of scope | Project uses **Java 21** |
| 5.1 Java 25 implications | Out of scope | — |

---

## Chapter 6 — Design considerations

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 6.1 Domain objects | **Covered** | [TUTORIAL-DICE.md](../TUTORIAL-DICE.md), `GitRepository` |
| 6.2 Tool call choice | Partial | Both MCP and `@LlmTool` exist; no decision guide |
| 6.3 Mixing LLMs | Partial | Single `embabel.models.default-llm`; no per-action model mix demo |

---

## Chapters 7–10

| Topic | Status | Notes |
|-------|--------|-------|
| 7. Contributing | Out of scope | Upstream Embabel project |
| 8. Resources | Partial | Linked from tutorials (blogs, Tripper, GOAP, DDD) |
| 8.1–8.5 | Partial | External links only |
| 9. Appendix | Out of scope | Official guide placeholder |
| 10. Planning module | Out of scope | Framework internals (A* GOAP, pruning) |

---

## Version gap

| | Official guide | simple-demo |
|---|----------------|-------------|
| Version | **0.5.0-SNAPSHOT** | **0.4.0** (`pom.xml`) |
| Blocked topics until upgrade | `@SecureAgentTool`, Agent Skills, fuller guardrail/MCP security docs, expanded DICE in guide | [TUTORIAL-SECURE-TOOLS.md](../TUTORIAL-SECURE-TOOLS.md) notes 0.5 APIs |

Bumping `embabel-agent.version` to **0.5.x** is the main unlock for guide topics that are already named in tutorials but not fully demonstrable in code.

---

## Suggested extensions (by effort)

### Low effort

- Document shell flags: `-p`, `-r`, `choose-goal`, `set-context`, open mode `-o`
- Add a short **troubleshooting** section (consolidate memory/MCP tables)
- One **`@Component` + `Ai`** example (guide §2.4)

### Medium effort (fits commit-message domain)

- Per-action **`LlmOptions`** (joke vs commit)
- **`ProcessOptions.toolCallContext`** (e.g. work-tree / tenant for tools)
- **`6.2`** prose: when to use MCP vs `withToolObject`
- **`CONCURRENT`** process type with parallel independent actions

### After 0.5 upgrade

- `@SecureAgentTool`, **Agent Skills**, richer **guardrails** on `PromptRunner`

### Keep out of this repo

- Bedrock / MiniMax / other cloud providers
- Custom SPI (`LlmService`, platform implementations)
- Hybrid / Supervisor planners (unless you add a dedicated tutorial agent)
- Planning module internals, Java 25 cgroup notes, IntelliJ plugin install steps

---

## Quick map: tutorials → guide areas

| Tutorial | Primary guide topics |
|----------|----------------------|
| [TUTORIAL.md](../TUTORIAL.md) | 2.3, 3.x, 4.1–4.3, 4.8, 4.22 |
| [TUTORIAL-JINJA.md](../TUTORIAL-JINJA.md) | 4.11 |
| [TUTORIAL-MEMORY.md](../TUTORIAL-MEMORY.md) | 4.13 |
| [TUTORIAL-MEMORY-SUMMARIZATION.md](../TUTORIAL-MEMORY-SUMMARIZATION.md) | 4.13 (context management) |
| [TUTORIAL-ROUTER.md](../TUTORIAL-ROUTER.md) | 4.6, 4.13, 4.20 (utility) |
| [TUTORIAL-TOOLS.md](../TUTORIAL-TOOLS.md) | 4.4, 4.9 |
| [TUTORIAL-RAG.md](../TUTORIAL-RAG.md) | 4.12 |
| [TUTORIAL-VECTOR-MEMORY.md](../TUTORIAL-VECTOR-MEMORY.md) | 4.12 (retrieval pattern) |
| [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) | 4.9, 4.33 |
| [TUTORIAL-TESTING.md](../TUTORIAL-TESTING.md) | 4.37 |
| [TUTORIAL-REST.md](../TUTORIAL-REST.md) | 4.18 |
| [TUTORIAL-INVOCATION.md](../TUTORIAL-INVOCATION.md) | 4.18 |
| [TUTORIAL-SECURE-TOOLS.md](../TUTORIAL-SECURE-TOOLS.md) | 4.30 (partial) |
| [TUTORIAL-EVAL.md](../TUTORIAL-EVAL.md) | 4.37 |
| [TUTORIAL-DICE.md](../TUTORIAL-DICE.md) | 1.1, 4.4, 6.1 |
| [TUTORIAL-A2A.md](../TUTORIAL-A2A.md) | 4.3, 4.6 (orchestration shape; not network A2A) |

---

See also: [EMBABEL_AGENT_GUIDE_TOPICS.md](EMBABEL_AGENT_GUIDE_TOPICS.md) · [SPRING_AI_REFERENCE_TOPICS.md](SPRING_AI_REFERENCE_TOPICS.md) · [SPRING_AI_GUIDE_COVERAGE.md](SPRING_AI_GUIDE_COVERAGE.md) · [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md) · [TUTORIAL-INDEX.md](../TUTORIAL-INDEX.md)
