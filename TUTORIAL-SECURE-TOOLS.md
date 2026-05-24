# Tutorial 26 — Secure tools and guardrails

Part of **`feat/all_together`** — see [TUTORIAL-INDEX.md](TUTORIAL-INDEX.md).

## Read-only git

`simple-demo.git.read-only=true` (default) makes [`GitExecutor`](src/main/java/com/example/simpledemo/git/GitExecutor.java) block mutating subcommands (`push`, `commit`, `reset`, …).

`GitRepository` tools only call allowlisted read commands.

## User-input guardrail

[`CommitSafetyGuardRail`](src/main/java/com/example/simpledemo/security/CommitSafetyGuardRail.java) rejects prompts that ask for `git push`, force push, etc.

Registered via [`DemoGuardRailConfiguration`](src/main/java/com/example/simpledemo/config/DemoGuardRailConfiguration.java).

## Production note

Embabel 0.5+ adds `@SecureAgentTool` for MCP/remote tools. This demo uses executor allowlisting + guardrails on 0.4.0.

## Try it

```bash
./mvnw test -Dtest=GitExecutorReadOnlyTest
```
