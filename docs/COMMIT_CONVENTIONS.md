# Commit message conventions (simple-demo)

This repository uses [Conventional Commits](https://www.conventionalcommits.org/) for subject lines.

## Subject line

- Format: `type(scope): imperative summary` (scope is optional)
- Types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`
- Max 72 characters, no trailing period
- Imperative mood: "add handler" not "added handler"

## Body

- Explain **what** changed and **why**, not how every line was edited
- Wrap lines at about 72 characters when the body is multi-line
- Reference tickets when applicable: `ESCA-1234`

## Examples

```
feat(agent): add Lucene RAG for commit style guide

Index project docs so commit suggestions follow house rules instead of
generic Conventional Commits boilerplate.
```

```
docs: document Tier 4 RAG and MCP tutorials
```

```
chore: bump embabel-agent to 0.4.0
```

## Anti-patterns

- `WIP`, `fix stuff`, `updates` as subjects
- Past tense subjects: `fixed bug in router`
- Bodies that only repeat the subject without added context
