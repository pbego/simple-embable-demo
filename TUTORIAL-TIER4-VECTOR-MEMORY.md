# Tier 4 — Vector memory (`feat/tier4-vector-memory`)

**Branch:** `feat/tier4-vector-memory` (includes everything on `feat/tier4-rag`)  
**Tutorial:** 20 — long-term vector memory for past commit suggestions

## Overview — why vector memory?

Tier 2 (on `feat/memory` / `feat/memory-summarization`) stores **full chat transcripts** on disk — great for “what did we say last Tuesday?” but weak for “find a commit message **like** this change” because similarity is not lexical.

**Vector memory** embeds each suggested `subject` + `body` and recalls the nearest neighbors before the next generation. That is a common production complement to transcript memory: RAG for **static docs**, vector store for **your own past outputs**.

| Concern | Tier 2 file chat (`feat/memory*`) | Tutorial 20 |
|---------|-----------------------------------|-------------|
| Full conversation | JSON per session | Not duplicated here |
| Repo conventions | — | RAG (previous branch) |
| Similar past commit | Keyword-poor | Cosine similarity on embeddings |

**Spring AI:** `SimpleVectorStore` with file persist; see `SpringAiVectorMemoryConfiguration` Javadoc for how `VectorStoreChatMemoryAdvisor` would wire a parallel `ChatClient` path.

## Checkout

```bash
git checkout feat/tier4-vector-memory
```

Requires RAG branch changes (Lucene + embeddings) plus vector memory beans.

## Prerequisites

Everything in [TUTORIAL-TIER4-RAG.md](TUTORIAL-TIER4-RAG.md), especially `nomic-embed-text`.

## Key code

| File | Role |
|------|------|
| `memory/CommitVectorMemory.java` | `remember` / `recallSimilar` |
| `config/VectorMemoryConfiguration.java` | `SimpleVectorStore` bean |
| `config/SpringAiVectorMemoryConfiguration.java` | Documents advisor pattern |
| `agent/CommitMessageAgent.java` | Recalls before LLM; remembers after |

## Configuration

```properties
simple-demo.vector-memory.enabled=true
simple-demo.vector-memory.recall-top-k=3
```

Persistence default: `~/.simple-demo/vector-memory.json` (via `SimpleVectorStore.save` / `load`).

Tests disable memory:

```properties
simple-demo.vector-memory.enabled=false
```

## Try it

1. Start the app (with RAG enabled and indexed):

```bash
./mvnw spring-boot:run
shell:> rag-index
```

2. Generate a commit twice on similar changes — the second prompt may include a **Similar past commits** block from the first suggestion.

```text
shell:> x "generate a commit message for my current changes"
# edit something small, run again
shell:> x "generate a commit message for my current changes"
```

3. Inspect the store path if needed: `~/.simple-demo/vector-memory.json`.

## How it interacts with RAG

| Source | What it retrieves |
|--------|-------------------|
| Lucene RAG | Team docs, conventions, sample commits (static) |
| Vector memory | Your agent’s prior suggestions (dynamic) |

Both are injected into `CommitMessageAgent` as separate prompt sections.

## Next branch

MCP consume/publish → [TUTORIAL-TIER4-MCP.md](TUTORIAL-TIER4-MCP.md) on `feat/tier4-mcp`.

Parent index: [TUTORIAL-TIER4.md](TUTORIAL-TIER4.md).
