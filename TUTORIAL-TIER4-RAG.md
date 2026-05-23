# Tier 4 — RAG & embeddings (`feat/tier4-rag`)

**Branch:** `feat/tier4-rag`  
**Tutorials:** 17 (local Lucene RAG), 18 (agentic RAG), 19 (Ollama embeddings)

## Overview — why RAG here?

`CommitMessageAgent` already sees **your current diff**. It does not automatically know **how this repository wants commits written** — that knowledge usually lives in `CONTRIBUTING.md`, team docs, or past good commits.

**Retrieval-Augmented Generation (RAG)** loads those sources into a searchable index. At commit time the agent pulls relevant chunks into the prompt (tutorial 17) or lets the LLM search repeatedly via tools (tutorial 18). **Embeddings** (tutorial 19) turn text into vectors so “conventional commit subject line” matches semantically, not only by keyword.

This mirrors production patterns: keep the LLM small, ground answers in verifiable docs, and avoid baking house style into Java strings.

| Concept | In this demo |
|---------|----------------|
| Index | Apache Lucene under `~/.simple-demo/lucene-index` |
| Corpus | `docs/COMMIT_CONVENTIONS.md`, `TUTORIAL.md`, `rag-sources/past-commits.sample.txt` |
| One-shot RAG | `CommitStyleRetriever` → prompt section in `CommitMessageAgent` |
| Agentic RAG | `ToolishRag` on `CommitStyleAgent` |
| Embeddings | Ollama `nomic-embed-text` via `ModelProvider` |

**Embabel guide:** RAG, Lucene module, Agentic RAG, `ToolishRag`, embeddings (ONNX optional).

## Checkout

```bash
git checkout feat/tier4-rag
```

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| Ollama `gemma4:e4b` | Chat / commit LLM |
| Ollama `nomic-embed-text` | Required for `rag-index` and vector search |
| Git | Commit agent |

```bash
ollama pull gemma4:e4b
ollama pull nomic-embed-text
```

## Key code

| File | Role |
|------|------|
| `config/RagConfiguration.java` | `LuceneSearchOperations` bean |
| `rag/CommitCorpusIngester.java` | Ingest markdown/text into Lucene |
| `rag/CommitStyleRetriever.java` | One-shot vector search for prompts |
| `agent/CommitStyleAgent.java` | Agentic RAG with `ToolishRag` |
| `shell/RagShellCommands.java` | `rag-index`, `rag-search` |
| `agent/CommitMessageAgent.java` | Injects retrieved style guide |

## Tutorial 17 — Local RAG (Lucene)

**Idea:** Build the index once; every commit generation gets top‑K chunks without the model managing search.

1. Run the app:

```bash
./mvnw spring-boot:run
```

2. Index sources:

```text
shell:> rag-index
```

3. Debug retrieval (no LLM):

```text
shell:> rag-search -q "conventional commits subject"
```

4. Generate a commit:

```text
shell:> x "generate a commit message for my current changes"
```

Configure sources in `application.properties`:

```properties
simple-demo.rag.sources=docs/COMMIT_CONVENTIONS.md,TUTORIAL.md,rag-sources/past-commits.sample.txt
simple-demo.rag.index-path=${user.home}/.simple-demo/lucene-index
```

Set `simple-demo.rag.enabled=false` in tests to skip Lucene/Ollama embeddings.

## Tutorial 18 — Agentic RAG

**Idea:** Instead of a fixed retrieval step, attach `ToolishRag` so the LLM decides when to call `vectorSearch`, `textSearch`, or expand results — better for open questions.

```text
shell:> x "how do we format commits in this repo?"
```

Chat route (keywords: convention, format + commit, style guide):

```text
shell:> chat
chat:> how do we format commits here?
```

If the index is empty, run `rag-index` first.

## Tutorial 19 — Embeddings (Ollama)

**Idea:** Lucene vector search needs an **embedding model**. This demo uses Ollama only (no ONNX artifacts in-repo).

```properties
embabel.models.default-embedding-model=nomic-embed-text
spring.ai.ollama.embedding.options.model=nomic-embed-text
```

`RagConfiguration` calls `modelProvider.getEmbeddingService()`. If startup or `rag-index` fails with “embedding service not found”, pull the model and restart.

For offline/CI embedding options, see the Embabel guide section on ONNX embeddings.

## Next branch

Semantic **memory of past commits** (not docs) is on `feat/tier4-vector-memory` → [TUTORIAL-TIER4-VECTOR-MEMORY.md](TUTORIAL-TIER4-VECTOR-MEMORY.md).

Parent index: [TUTORIAL-TIER4.md](TUTORIAL-TIER4.md).
