# Tutorial 25 — Programmatic invocation

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## Shell

```text
embabel> commit-now "DOC-2: summarize staged API changes"
```

Uses [`CommitInvocationRunner`](src/main/java/com/example/simpledemo/invocation/CommitInvocationRunner.java) and `AgentInvocation.create(agentPlatform, CommitMessage.class)`.

## From Java

```java
@Autowired CommitInvocationRunner runner;

var proposal = runner.run("fix: handle null in router");
System.out.println(proposal.formatted());
```

## CI

[`.github/workflows/suggest-commit.yml`](.github/workflows/suggest-commit.yml) builds the jar and runs `commit-now` with mock-friendly properties.

For production CI, pin `embabel.agent.platform.test.mock-mode=true` when no LLM is available, or call Ollama in a self-hosted runner.
