# Jinja Prompts in the Commit Message Agent

This supplement explains how [Jinja2](https://jinja.palletsprojects.com/)-style templates (rendered with **Jinjava** on the JVM) are used in this project alongside Embabel.

The main tutorial is [TUTORIAL.md](TUTORIAL.md).

## Why Jinja here?

The commit message agent has two steps:

1. **Java** — `GitChangesCollector` runs `git` and builds a `GitChanges` object.
2. **LLM** — Embabel sends a **prompt** to Ollama and parses a `CommitMessage`.

Step 2 used to build the prompt as a Java text block. That works for demos but gets hard to maintain when you add conditionals, shared rules, or copy edits.

**Jinja moves the prompt to resource files** under `src/main/resources/prompts/`, which Embabel loads and renders before calling the model.

## Files

```
src/main/resources/prompts/commit/
├── generate_message.jinja      # Main prompt (used by the agent)
└── _conventional_rules.jinja   # Shared fragment ({% include %})
```

Embabel resolves `commit/generate_message` as  
`classpath:/prompts/commit/generate_message.jinja`  
(the `.jinja` suffix is added automatically).

## Java wiring

In `CommitMessageAgent.generateCommitMessage`:

```java
return ai.withDefaultLlm()
    .rendering("commit/generate_message")
    .createObject(
        CommitMessage.class,
            Map.of(
                "branch", changes.branch(),
                "status", JinjavaSafe.escape(changes.status()),
                "changeSections", buildChangeSections(changes),
                "developerSection", buildDeveloperSection(developerHint)));
```

| Piece | Role |
|-------|------|
| `.rendering("commit/generate_message")` | Select template (under `prompts/`) |
| `Map.of(...)` | Variables available in the template as `{{ branch }}`, etc. |
| `createObject(CommitMessage.class, ...)` | Same structured JSON output as before |

This matches the pattern in [Embabel’s template docs](https://docs.embabel.com/embabel-agent/guide/0.1.3/) and the `FactChecker` example in [embabel-agent-examples](https://github.com/embabel/embabel-agent-examples).

## Template walkthrough

### Variables from Java

| Variable | Source |
|----------|--------|
| `branch` | `git branch --show-current` |
| `status` | `git status --short` |
| `stagedDiff` | `git diff --staged` |
| `unstagedDiff` | `git diff` |
| `changeSections` | Staged + unstaged diff blocks (built and escaped in Java) |
| `developerSection` | Optional hint block, or empty string |

### Git diffs and nested Jinja (important)

When your diff includes `.jinja` files, the patch text contains literal `{% if %}`, `{{`, etc.  
If you put that raw text in `{{ stagedDiff }}`, **Jinjava parses it again** and throws
`Syntax error in '{% if %}'`.

This project avoids that by:

1. Building `changeSections` and `developerSection` in **Java** (no `{% if %}` in the template).
2. Running diffs through `JinjavaSafe.escape()` so `{%` / `{{` are not interpreted as tags.

The template only uses `{% include %}` for static rules and plain variables:

```jinja
{{ changeSections }}
{{ developerSection }}
```

### Includes

Shared instructions live in one place:

```jinja
{% include "commit/_conventional_rules.jinja" %}
```

Edit `_conventional_rules.jinja` to change Conventional Commits rules for every prompt that includes it.

## Try it

Run the app as in [TUTORIAL.md](TUTORIAL.md):

```bash
./mvnw spring-boot:run
```

```text
x "generate a commit message for my current changes"
```

To see the **rendered** prompt sent to the model:

```text
x "generate commit message" -p
```

Edit `generate_message.jinja`, save, and run again (DevTools restart picks up classpath changes).

## Customizing prompts

| Goal | What to do |
|------|------------|
| Stricter subject line | Edit `_conventional_rules.jinja` |
| Shorter prompts (smaller models) | Trim sections or pass a diff stat from Java instead of full diff |
| Team template | Add `prompts/commit/team_acme.jinja` and use `.rendering("commit/team_acme")` |
| Different template per profile | `@Value` or Spring profile + constant for template name |

## Jinja vs Java for this agent

| Concern | Keep in Java | Keep in Jinja |
|---------|----------------|---------------|
| Run `git` | Yes (`GitChangesCollector`) | No |
| Agent flow / types | Yes (`@Agent`, `@Action`) | No |
| Prompt wording & layout | Optional | Yes |
| `if` / `for` / includes | Awkward | Natural |

## Troubleshooting

| Issue | Check |
|-------|--------|
| Template not found | Path is relative to `prompts/` without `.jinja`; file is under `src/main/resources/prompts/` |
| Variable empty in output | Key in `Map.of` must match `{{ name }}` in template exactly |
| Include fails | Path in `{% include %}` is relative to `prompts/` (e.g. `commit/_conventional_rules.jinja`) |
| Still short commit body | Tune template (“Write a complete commit body…”) or model; Jinja only changes the prompt text |
| `{% if %}` syntax error | Diff contained `.jinja` tags; ensure `JinjavaSafe.escape()` is applied (see above) |

## Further reading

- [Embabel reference: Templates](https://docs.embabel.com/embabel-agent/guide/0.1.3/)
- [Jinjava](https://github.com/HubSpot/jinjava) (Jinja-compatible engine used on the JVM)
- Example: `prompts/factchecker/consolidate_assertions.jinja` in embabel-agent-examples
