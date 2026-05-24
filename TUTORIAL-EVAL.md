# Tutorial 27 — Eval / regression

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## Golden cases

[`src/test/resources/eval/commit-golden.json`](src/test/resources/eval/commit-golden.json) lists sample `status` / `stagedDiff` inputs and `expectedSubjectPattern` regexes.

[`CommitMessageGoldenEvalTest`](src/test/java/com/example/simpledemo/eval/CommitMessageGoldenEvalTest.java) runs them as a parameterized JUnit test with a mocked LLM.

```bash
./mvnw test -Dtest=CommitMessageGoldenEvalTest
```

## Upgrade path

On Embabel 0.5.x, consider the `embabel-agent-eval` module for richer eval harnesses. This branch keeps a plain JUnit golden suite for 0.4.0.
