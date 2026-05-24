# Git tools

Let the LLM call **real git operations** via `@Tool` on a `GitRepository` domain object instead of hallucinating branch names or diffs.

## Overview

`CommitMessageAgent` runs git in Java and passes text to the model. **`GitInfoAgent`** exposes methods like list branches, show status, and inspect commits as **tools** the model invokes during a turn.

This is the Embabel pattern for **domain `@Tool`** objects: structured, testable code the LLM can call safely (often combined with **tool groups** in later work).

**Embabel guide:** Domain objects + `@Tool`, tool groups.

## Prerequisites

Same as `main`. Run from a git repository (or set `simple-demo.git.work-tree`).

## Key code

| File | Role |
|------|------|
| `agent/GitInfoAgent.java` | LLM + `withToolObject(repository)` |
| `git/GitRepository.java` | `@Tool` methods |
| `git/GitExecutor.java` | Runs `git -C` |

## Try it

```bash
./mvnw spring-boot:run
```

```text
shell:> x "what branch am I on?"
shell:> x "summarize the last three commits"
shell:> x "which files changed compared to main?"
```

The model should call git tools rather than inventing output.

## Related branches

- `feat/tier4-rag` — ground answers in repo docs
- `feat/tier4-mcp` — external MCP tools alongside local git tools

Branch index on `main`: [README.md](https://github.com/pbego/simple-embable-demo/blob/main/README.md#feature-branches).
