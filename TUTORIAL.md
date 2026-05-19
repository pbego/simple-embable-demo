# Embabel + Ollama — Git Commit Message Agent

This project is a minimal [Embabel](https://docs.embabel.com/embabel-agent/guide/0.1.3/) agent that runs on your machine with [Ollama](https://ollama.com/) (**gemma4:e4b**). It reads **real git changes** from your repo and suggests a **Conventional Commits**-style message. No cloud API keys required.

## What we built

- **Spring Boot** + Embabel (`embabel-agent-starter-ollama`, `embabel-agent-starter-shell`)
- **CommitMessageAgent** with two steps:
  1. **collectChanges** — runs `git` locally (branch, status, staged/unstaged diffs) → `GitChanges` (**no LLM**)
  2. **generateCommitMessage** — sends diffs to Ollama → `CommitMessage` (`@AchievesGoal`)

```
UserInput (shell)  →  collectChanges (git)  →  GitChanges  →  generateCommitMessage (LLM)  →  CommitMessage
```

This mixes **code agency** (git) with **LLM agency** (wording the commit), which is a typical Embabel pattern.

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **Java 21** | Set in `pom.xml` |
| **Maven** | `./mvnw` |
| **Git** | Repo with changes; run the app from the repo root (or set `simple-demo.git.work-tree`) |
| **Ollama** | `http://localhost:11434` |
| **gemma4:e4b** | `ollama pull gemma4:e4b` |

## Project layout

```
simple-demo/
├── pom.xml
├── TUTORIAL.md
└── src/main/java/com/example/simpledemo/
    ├── SimpleDemoApplication.java
    ├── agent/
    │   ├── CommitMessageAgent.java
    │   ├── CommitMessage.java
    │   └── GitChanges.java
    ├── git/
    │   └── GitChangesCollector.java   # runs git CLI
    └── resources/prompts/commit/
        ├── generate_message.jinja     # LLM prompt (Jinja)
        └── _conventional_rules.jinja  # shared fragment
```

## Configuration

| Property | Default | Purpose |
|----------|---------|---------|
| `spring.ai.ollama.base-url` | `http://localhost:11434` | Ollama |
| `embabel.models.default-llm` | `gemma4:e4b` | Model for commit message generation |
| `simple-demo.git.work-tree` | `.` | Directory passed to `git -C` (use repo root) |

## Run and try

From your **git repository** (this project or any repo — set `simple-demo.git.work-tree` if needed):

```bash
./mvnw spring-boot:run
```

In the shell:

```text
agents
x "generate a commit message for my current changes"
```

Optional hints in the same command:

```text
x "suggest commit message, focus on the API changes and use fix: prefix"
```

Output is JSON with `subject` and `body` you can paste into `git commit`.

## How it works

### Step 1: `GitChangesCollector`

Runs:

- `git branch --show-current`
- `git status --short`
- `git diff --staged`
- `git diff`

Diffs are truncated at 12,000 characters each so local models stay within context.

### Step 2: LLM (Jinja prompt)

The prompt is rendered from `src/main/resources/prompts/commit/generate_message.jinja` (Jinjava), then sent to Ollama. Variables: branch, status, diffs, optional hint. See **[TUTORIAL_JINJA.md](TUTORIAL_JINJA.md)** for how templates, `{% if %}`, and `{% include %}` work.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Empty or wrong diffs | Run from repo root or set `simple-demo.git.work-tree=/path/to/repo` |
| `git command failed` | Ensure `git` is on `PATH` and the path is a git work tree |
| Bad commit message | Add hints in `x "..."`; try a larger model in Ollama |
| Default LLM not found | Match `embabel.models.default-llm` to `ollama list` exactly |

## Tests

```bash
./mvnw test
```

Context-load only; no live Ollama or git required in CI.

## Further reading

- [Embabel User Guide](https://docs.embabel.com/embabel-agent/guide/0.1.3/)
- [Embabel examples](https://github.com/embabel/embabel-agent-examples)
