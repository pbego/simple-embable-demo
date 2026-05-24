# Tutorial 29 — Multi-agent orchestration (A2A shape)

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## In-process pipeline

This branch demonstrates multi-specialist workflows **without** `embabel-agent-a2a` (0.4.0):

1. [`SecurityReviewAgent`](src/main/java/com/example/simpledemo/agent/SecurityReviewAgent.java) — scan diffs
2. [`ChangelogAgent`](src/main/java/com/example/simpledemo/agent/ChangelogAgent.java) — user-facing summary
3. [`CommitMessageAgent`](src/main/java/com/example/simpledemo/agent/CommitMessageAgent.java) — final proposal

[`CommitOrchestratorAgent`](src/main/java/com/example/simpledemo/agent/CommitOrchestratorAgent.java) chains them via `@Action` steps and `@AchievesGoal`.

## Chat

```text
chat:> @orchestrate generate a commit for my staged changes
```

[`ChatRouter`](src/main/java/com/example/simpledemo/chat/ChatRouter.java) route `ORCHESTRATE` runs the pipeline and returns security notes, changelog, and commit text.

## Shell

```text
x "orchestrate a commit message with security review"
```

Autonomy should select `CommitOrchestratorAgent`.

## Production

Google A2A / `embabel-agent-a2a` adds network transport between agents. The workflow shape here maps directly to separate remote specialists later.
