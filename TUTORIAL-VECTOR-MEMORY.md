# Vector memory

Recall **similar past commit suggestions** with Spring AI `SimpleVectorStore` (builds on RAG in `feat/tier4-rag`).

## Overview

`feat/memory` stores **full chat transcripts** on disk — great for “what did we say last Tuesday?” but weak for “find a commit message **like** this change” because similarity is not lexical.

**Vector memory** embeds each suggested `subject` + `body` and recalls the nearest neighbors before the next generation. Use RAG for **static docs**; use vector memory for **your own past outputs**.

| Concern | File chat (`feat/memory`) | This branch |
|---------|---------------------------|-------------|
| Full conversation | JSON per session | Not duplicated here |
| Repo conventions | — | Lucene RAG |
| Similar past commit | Keyword-poor | Embedding similarity |

**Spring AI:** see `SpringAiVectorMemoryConfiguration` Javadoc for a `VectorStoreChatMemoryAdvisor` pattern on a separate `ChatClient`.

## Prerequisites

Same as `TUTORIAL-RAG.md` on `feat/tier4-rag` (`nomic-embed-text`, `rag-index`).

## Key code

| File | Role |
|------|------|
| `memory/CommitVectorMemory.java` | `remember` / `recallSimilar` |
| `config/VectorMemoryConfiguration.java` | `SimpleVectorStore` bean |
| `agent/CommitMessageAgent.java` | Recalls before LLM; remembers after |

## Configuration

```properties
simple-demo.vector-memory.enabled=true
simple-demo.vector-memory.recall-top-k=3
```

Store file: `~/.simple-demo/vector-memory.json`.

## Try it

```bash
./mvnw spring-boot:run
shell:> rag-index
shell:> x "generate a commit message for my current changes"
# small edit, run again — prompt may include "Similar past commits"
```

## Next

MCP consume/publish: `feat/tier4-mcp`, `TUTORIAL-MCP.md`.

Branch index on `main`: [README.md](https://github.com/pbego/simple-embable-demo/blob/main/README.md#feature-branches).
