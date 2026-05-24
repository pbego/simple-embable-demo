# Tutorial index — feat/all_together

All topics below live on **`feat/all_together`**. Start with [README.md](README.md), then open the tutorial for each topic.

**Guide alignment:** [docs/GUIDE_COVERAGE.md](docs/GUIDE_COVERAGE.md) maps these tutorials to the [official Embabel guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) (see [docs/EMBABEL_AGENT_GUIDE_TOPICS.md](docs/EMBABEL_AGENT_GUIDE_TOPICS.md) for topic summaries).

| # | Topic | Tutorial |
|---|--------|----------|
| — | Baseline shell, `x`, chat | [TUTORIAL.md](TUTORIAL.md) |
| — | Jinja commit prompts | [TUTORIAL-JINJA.md](TUTORIAL-JINJA.md) |
| — | File-backed chat memory | [TUTORIAL-MEMORY.md](TUTORIAL-MEMORY.md) |
| — | Session summarization | [TUTORIAL-MEMORY-SUMMARIZATION.md](TUTORIAL-MEMORY-SUMMARIZATION.md) |
| — | LLM chat router | [TUTORIAL-ROUTER.md](TUTORIAL-ROUTER.md) |
| — | Git `@Tool` agent | [TUTORIAL-TOOLS.md](TUTORIAL-TOOLS.md) |
| — | Lucene RAG | [TUTORIAL-RAG.md](TUTORIAL-RAG.md) |
| — | Vector memory | [TUTORIAL-VECTOR-MEMORY.md](TUTORIAL-VECTOR-MEMORY.md) |
| — | MCP consume & publish | [TUTORIAL-MCP.md](TUTORIAL-MCP.md) |
| 23 | Unit + integration tests | [TUTORIAL-TESTING.md](TUTORIAL-TESTING.md) |
| 24 | REST + SSE process API | [TUTORIAL-REST.md](TUTORIAL-REST.md) |
| 25 | Programmatic invocation | [TUTORIAL-INVOCATION.md](TUTORIAL-INVOCATION.md) |
| 26 | Secure tools & guardrails | [TUTORIAL-SECURE-TOOLS.md](TUTORIAL-SECURE-TOOLS.md) |
| 27 | Eval / regression | [TUTORIAL-EVAL.md](TUTORIAL-EVAL.md) |
| 28 | DICE / typed context | [TUTORIAL-DICE.md](TUTORIAL-DICE.md) |
| 29 | Multi-agent orchestration | [TUTORIAL-A2A.md](TUTORIAL-A2A.md) |

## Profiles

| Profile | Purpose |
|---------|---------|
| *(default)* | Embabel shell + full stack |
| `mcp` | Filesystem MCP client |
| `mcp-server` | Publish agents on SSE :8081 |
| `api` | REST trigger + platform process SSE :8080 |
