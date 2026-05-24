# Tutorial 28 — DICE / typed domain context

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## Typed records

| Type | Role |
|------|------|
| `CommitRequest` | Developer hint |
| `RepositorySnapshot` | Branch, status, diffs |
| `StyleGuideContext` | RAG conventions |
| `SimilarCommitsContext` | Vector recall |
| `CommitProposal` | LLM output |

Package: [`com.example.simpledemo.domain`](src/main/java/com/example/simpledemo/domain/).

## Agent flow

[`CommitMessageAgent`](src/main/java/com/example/simpledemo/agent/CommitMessageAgent.java) exposes planner steps:

1. `captureRepository` → `RepositorySnapshot`
2. `loadStyleGuide` / `loadSimilarCommits`
3. `proposeCommit` → `CommitProposal`

Shell `x` still works via `collectChanges` → `generateCommitMessage(GitChanges, …)`.

## Further reading

[DICE in the Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) — full module on 0.5.x.
