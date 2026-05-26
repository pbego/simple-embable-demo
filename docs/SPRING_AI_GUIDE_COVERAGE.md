# Spring AI guide coverage — simple-demo vs official topics

This matrix maps [SPRING_AI_REFERENCE_TOPICS.md](SPRING_AI_REFERENCE_TOPICS.md) (summaries of the [Spring AI reference](https://docs.spring.io/spring-ai/reference/index.html)) to what **simple-demo** demonstrates, and how that relates to **Embabel**.

**How the stacks fit together:** [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md)  
**Embabel guide coverage:** [GUIDE_COVERAGE.md](GUIDE_COVERAGE.md)

| Status | Meaning |
|--------|---------|
| **Covered** | Runnable behavior and/or explicit config in this repo |
| **Partial** | Transitive via Embabel, prose/Javadoc only, or subset of the topic |
| **Not covered** | Not demonstrated; could be added |
| **Embabel layer** | Implemented by Embabel APIs/patterns instead of raw Spring AI (still “addressed” for agent apps) |

**Repo:** Embabel **0.4.0** (transitive Spring AI via `embabel-agent-dependencies`), Java **21**, **Ollama-only**. Spring AI is rarely imported in application code except vector memory and MCP types.

---

## Summary

| Status | ~Topics |
|--------|---------|
| Covered | 8 |
| Partial | 14 |
| Embabel layer | 12 |
| Not covered | 22+ |

Most **agent-facing** Spring AI topics (ChatClient, Advisors, modular RAG advisors) are **Embabel layer** in this repo: Embabel’s `Ai`, tool loop, and Lucene RAG cover the same problem space with different APIs.

---

## Chapter 1 — Introduction

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 1. Introduction | Partial | [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md); [README.md](../README.md) positions Embabel, not Spring AI marketing copy |

---

## Chapter 2 — AI Concepts

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 2.1 Models | Partial | Ollama chat + embeddings only; via Embabel + `spring.ai.ollama.*` |
| 2.2 Prompts | **Embabel layer** | `Ai`, `UserMessage` / roles in [TUTORIAL-ROUTER.md](../TUTORIAL-ROUTER.md); not raw `Prompt` API |
| 2.3 Prompt Templates | **Embabel layer** | Jinja via `PromptRunner.rendering()` — [TUTORIAL-JINJA.md](../TUTORIAL-JINJA.md) |
| 2.4 Embeddings | **Covered** | RAG + `EmbeddingModel` / `SimpleVectorStore` — [TUTORIAL-RAG.md](../TUTORIAL-RAG.md), [TUTORIAL-VECTOR-MEMORY.md](../TUTORIAL-VECTOR-MEMORY.md) |
| 2.5 Tokens | Not covered | No token budgeting tutorial |
| 2.6 Structured Output | **Embabel layer** | `createObject(CommitMessage.class)` etc.; not `BeanOutputConverter` |
| 2.7 Bringing data & APIs | Partial | RAG + tools + MCP — split across tutorials |
| 2.8 RAG (concept) | **Covered** | Lucene (Embabel) + vector recall (Spring AI store) |
| 2.9 Tool Calling (concept) | **Embabel layer** | `@LlmTool`, MCP tool groups — [TUTORIAL-TOOLS.md](../TUTORIAL-TOOLS.md), [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) |
| 2.10 Evaluating responses | Partial | Golden JUnit suite — [TUTORIAL-EVAL.md](../TUTORIAL-EVAL.md); not Spring AI `RelevancyEvaluator` |

---

## Chapter 3 — Getting Started

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 3.1 Spring Initializr | Not covered | Repo is the sample; no Initializr walkthrough |
| 3.2 Artifact Repositories | Partial | Spring milestones repo in [pom.xml](../pom.xml); releases via Embabel BOM |
| 3.3 Dependency Management | Partial | `embabel-agent-dependencies` BOM imports Spring AI versions |
| 3.4 Add dependencies | Partial | Embabel starters only in `pom.xml`; no direct `spring-ai-*` coordinates |
| 3.5 Spring AI samples | Not covered | Links only via [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md) |

---

## Chapter 4 — Reference

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 4.1 Chat Client | **Embabel layer** | `Ai` / chatbot instead of app-level `ChatClient` |
| 4.2 Prompts (API) | **Embabel layer** | See 2.2–2.3 |
| 4.3 Structured Output | **Embabel layer** | `createObject()` in agents |
| 4.4 Multimodality | Not covered | Text-only |
| 4.5 Models | **Partial** | Ollama chat + embeddings only (no OpenAI/Azure/Bedrock pages) |
| 4.6 Chat Memory | **Embabel layer** | File `Conversation` store — [TUTORIAL-MEMORY.md](../TUTORIAL-MEMORY.md); Spring `ChatMemory` advisors not wired |
| 4.7 Tool Calling | **Embabel layer** | `@LlmTool`, MCP; not Spring `@Tool` on `ChatClient` |
| 4.8 MCP | **Covered** | `mcp` / `mcp-server` profiles — [TUTORIAL-MCP.md](../TUTORIAL-MCP.md); `spring.ai.mcp.*` + Embabel `McpToolGroup` |
| 4.9 RAG | **Covered** (split) | **Embabel:** Lucene — [TUTORIAL-RAG.md](../TUTORIAL-RAG.md); **Spring AI:** `SimpleVectorStore` memory — [TUTORIAL-VECTOR-MEMORY.md](../TUTORIAL-VECTOR-MEMORY.md); not `QuestionAnswerAdvisor` |
| 4.10 Model Evaluation | Partial | Custom golden eval; not `Evaluator` API |
| 4.11 Vector Stores | **Partial** | `SimpleVectorStore` only; not PGVector/Redis/etc. |
| 4.12 Observability | Partial | Spring/Embabel metrics possible via starters; no dedicated tutorial |
| 4.13 Development-time Services | Not covered | No Docker Compose profile |
| 4.14 Testcontainers | Not covered | — |

**Also in repo (Spring AI touchpoints):**

| Code / config | Spring AI types |
|---------------|-----------------|
| [VectorMemoryConfiguration.java](../src/main/java/com/example/simpledemo/config/VectorMemoryConfiguration.java) | `EmbeddingModel`, `SimpleVectorStore` |
| [CommitVectorMemory.java](../src/main/java/com/example/simpledemo/memory/CommitVectorMemory.java) | `Document`, `SearchRequest`, `SimpleVectorStore` |
| [application.properties](../src/main/resources/application.properties) | `spring.ai.ollama.*`, embedding model |
| [application-mcp.properties](../src/main/resources/application-mcp.properties) | `spring.ai.mcp.client.*` |
| [application-mcp-server.properties](../src/main/resources/application-mcp-server.properties) | `spring.ai.mcp.server.*` |
| [DemoMcpToolGroupsConfiguration.java](../src/main/java/com/example/simpledemo/config/DemoMcpToolGroupsConfiguration.java) | `McpSyncClient` (Spring AI MCP autoconfig) |
| [SpringAiVectorMemoryConfiguration.java](../src/main/java/com/example/simpledemo/config/SpringAiVectorMemoryConfiguration.java) | Javadoc for `VectorStoreChatMemoryAdvisor` pattern (not active) |

---

## Chapter 5 — Guides

| Topic | Status | Where in simple-demo |
|-------|--------|----------------------|
| 5.1 Awesome Spring AI | Not covered | External link |
| 5.2 Getting Started with MCP | **Covered** | [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) (Embabel + Spring AI config) |
| 5.3 Dynamic Tool Discovery | Not covered | Community `tool-search-tool` not used |
| 5.4 LLM-as-a-Judge | Not covered | — |
| 5.5 Prompt Engineering Patterns | Partial | Jinja + router prompts; no pattern catalog doc |
| 5.6 Building Effective Agents | Partial | Multi-agent orchestration — [TUTORIAL-A2A.md](../TUTORIAL-A2A.md); not Anthropic workflow code samples |

---

## Chapter 6 — Upgrade Notes

| Topic | Status | Notes |
|-------|--------|-------|
| 6.1 ToolCallback migration | Partial | Embabel tool loop abstracts callbacks; app does not use legacy `FunctionCallback` |
| 6.2 Anthropic SDK migration | Out of scope | Ollama-only |

---

## Embabel vs Spring AI — topic mapping

Use this when you know a Spring AI feature and want the Embabel equivalent in this repo.

| Spring AI reference topic | In simple-demo |
|---------------------------|----------------|
| ChatClient fluent API | Embabel `Ai`, `AgentProcessChatbot`, shell `chat` |
| Advisors (memory, RAG) | Embabel `PersistingConversation`, `CommitStyleRetriever`, `CommitVectorMemory` |
| `@Tool` / ToolCallback | `@LlmTool`, `GitRepository`, MCP `ToolGroup` |
| QuestionAnswerAdvisor / RetrievalAugmentationAdvisor | Embabel Lucene `ToolishRag` + `CommitStyleRetriever` |
| ETL Pipeline | `CommitCorpusIngester`, `rag-index` (Embabel readers + Lucene) |
| ChatMemory | JSON file conversations under `~/.simple-demo/conversations` |
| MCP client/server starters | `spring.ai.mcp.*` + Embabel `McpToolGroup` / MCP server autoconfig |
| VectorStore (portable) | `SimpleVectorStore` for past commits only |
| Model Evaluation | JUnit golden tests — [TUTORIAL-EVAL.md](../TUTORIAL-EVAL.md) |
| Observability | Incidental via Spring Boot; not demonstrated |

---

## Suggested extensions (Spring AI–focused)

### Low effort

- Enable **`spring-ai-advisors-vector-store`** + profile `spring-ai-memory` per [SpringAiVectorMemoryConfiguration](../src/main/java/com/example/simpledemo/config/SpringAiVectorMemoryConfiguration.java) Javadoc
- Short doc section: **`spring.ai.*` vs `embabel.*`** (consolidate [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md) into TUTORIAL.md)

### Medium effort

- One minimal **`ChatClient`** `@RestController` beside Embabel REST for side-by-side comparison
- **`RelevancyEvaluator`** integration test on RAG answers
- **Docker Compose** profile for Ollama + optional Chroma/Qdrant

### Keep out of this repo (unless scope changes)

- Cloud provider starters (OpenAI, Bedrock, Vertex, …)
- Image / audio / moderation models
- Per–vector-database starter catalog demos
- Spring AI “effective agents” example port (already have Embabel orchestration)

---

## Quick map: tutorials → Spring AI topics

| Tutorial | Spring AI topics | Embabel topics |
|----------|------------------|----------------|
| [TUTORIAL.md](../TUTORIAL.md) | Ollama config (partial) | Shell, agents, autonomy |
| [TUTORIAL-RAG.md](../TUTORIAL-RAG.md) | Embeddings concept | Lucene RAG module |
| [TUTORIAL-VECTOR-MEMORY.md](../TUTORIAL-VECTOR-MEMORY.md) | **VectorStore**, **EmbeddingModel**, ETL-like ingest | Agent augments prompt |
| [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) | **MCP** client/server | Tool groups, MCP server export |
| [TUTORIAL-MEMORY.md](../TUTORIAL-MEMORY.md) | — (Embabel file memory) | Chatbot / conversation |
| [TUTORIAL-TOOLS.md](../TUTORIAL-TOOLS.md) | Tool calling concept | `@LlmTool` |
| [TUTORIAL-EVAL.md](../TUTORIAL-EVAL.md) | Evaluation concept | Golden tests |
| [TUTORIAL-REST.md](../TUTORIAL-REST.md) | — | Process API / SSE |

---

See also: [SPRING_AI_REFERENCE_TOPICS.md](SPRING_AI_REFERENCE_TOPICS.md) · [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md) · [GUIDE_COVERAGE.md](GUIDE_COVERAGE.md) · [TUTORIAL-INDEX.md](../TUTORIAL-INDEX.md)
