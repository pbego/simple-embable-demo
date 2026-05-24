# RAG & embeddings

Lucene-backed retrieval over project docs, plus Ollama embeddings for semantic search.

## Overview

`CommitMessageAgent` already sees **your current diff**. It does not automatically know **how this repository wants commits written** — that knowledge usually lives in `CONTRIBUTING.md`, team docs, or past good commits.

**Retrieval-Augmented Generation (RAG)** loads those sources into a searchable index. At commit time the agent pulls relevant chunks into the prompt (**one-shot RAG**) or lets the LLM search repeatedly via tools (**agentic RAG** on `CommitStyleAgent`). **Embeddings** turn text into vectors so “conventional commit subject line” matches semantically, not only by keyword.

| Concept | In this branch |
|---------|----------------|
| Index | Apache Lucene under `~/.simple-demo/lucene-index` |
| Corpus | `docs/COMMIT_CONVENTIONS.md`, `TUTORIAL.md`, `rag-sources/past-commits.sample.txt` |
| One-shot RAG | `CommitStyleRetriever` → prompt section in `CommitMessageAgent` |
| Agentic RAG | `ToolishRag` on `CommitStyleAgent` |
| Embeddings | Ollama `nomic-embed-text` via `ModelProvider` |

**Embabel guide:** RAG, Lucene module, Agentic RAG, `ToolishRag`, embeddings (ONNX optional).

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

## Local RAG (Lucene)

Build the index once; every commit generation gets top‑K chunks without the model managing search.

```bash
./mvnw spring-boot:run
```

```text
shell:> rag-index
shell:> rag-search -q "conventional commits subject"
shell:> x "generate a commit message for my current changes"
```

```properties
simple-demo.rag.sources=docs/COMMIT_CONVENTIONS.md,TUTORIAL.md,rag-sources/past-commits.sample.txt
simple-demo.rag.index-path=${user.home}/.simple-demo/lucene-index
embabel.models.default-embedding-model=nomic-embed-text
spring.ai.ollama.embedding.options.model=nomic-embed-text
```

## Agentic RAG

Attach `ToolishRag` so the LLM decides when to call `vectorSearch` / `textSearch`:

```text
shell:> x "how do we format commits in this repo?"
```

Chat (`ChatRouter` routes convention-style questions to `CommitStyleAgent`):

```text
shell:> chat
chat:> how do we format commits here?
```

Run `rag-index` first if the index is empty.

## Next

Semantic memory of past commits: branch `feat/tier4-vector-memory`, file `TUTORIAL-VECTOR-MEMORY.md`.

Branch index on `main`: [README.md](https://github.com/pbego/simple-embable-demo/blob/main/README.md#feature-branches).
