# Spring AI Reference — Topics

Source: [Spring AI Reference](https://docs.spring.io/spring-ai/reference/index.html)

Topics are listed up to the second heading level in the published guide (`x` and `x.x`). Each entry has a short summary (one or two sentences). Provider-specific pages (individual chat models, vector DBs, etc.) are grouped under their parent topic rather than listed separately.

**Coverage in this repo:** [SPRING_AI_GUIDE_COVERAGE.md](SPRING_AI_GUIDE_COVERAGE.md) — what simple-demo demonstrates vs gaps. **Embabel + Spring AI:** [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md).

---

## 1. Introduction

Introduces Spring AI as a Java framework for building AI applications with portable abstractions across models, vector stores, tools, advisors, RAG, and observability—without porting Python stacks like LangChain wholesale.

---

## 2. AI Concepts

Describes core ideas behind Spring AI’s design: models, prompts, embeddings, tokens, structured output, RAG, tool calling, and evaluation. Recommended reading before diving into the API reference.

### 2.1. Models

Explains AI models by input/output modality (language, image, audio, embeddings) and how Spring AI supports chat, image, audio, and embedding use cases on the JVM.

### 2.2. Prompts

Covers prompts as structured, role-based message sequences (system, user, assistant, tool)—not just a single string—and why prompt engineering matters for quality.

### 2.3. Prompt Templates

Describes `StringTemplate`-based templates with placeholders, analogous to Spring MVC views, for building dynamic prompts from user input and application data.

### 2.4. Embeddings

Explains embeddings as numeric vectors that capture semantic similarity, enabling semantic search and RAG by locating related content in vector space.

### 2.5. Tokens

Summarizes how models tokenize text, how token limits define context windows, and how token usage drives billing and batching strategies for large documents.

### 2.6. Structured Output

Introduces the challenge of getting reliable JSON/typed results from LLMs and the converter-based approach Spring AI uses before and after model calls.

### 2.7. Bringing Your Data & APIs to the AI Model

Frames the three main ways to extend models beyond training data: fine-tuning, prompt stuffing (RAG), and tool calling.

### 2.8. Retrieval Augmented Generation

Describes the RAG pattern: ingest documents into a vector store, retrieve similar chunks at query time, and augment the prompt so the model answers from your data.

### 2.9. Tool Calling

Explains how models request client-side tools, how Spring AI handles the tool-call conversation loop, and how `@Tool`-annotated methods connect LLMs to live APIs and data.

### 2.10. Evaluating AI responses

Introduces using models (and vector context) to judge relevance and quality of generated answers, which Spring AI exposes through its `Evaluator` API.

---

## 3. Getting Started

Jumping-off points for bootstrapping a Spring Boot project with Spring AI: Initializr, repositories, BOM, dependencies, and sample projects.

### 3.1. Spring Initializr

Use [start.spring.io](https://start.spring.io) to generate a project with selected AI models and vector stores pre-wired in the build.

### 3.2. Artifact Repositories

Documents Maven Central for releases (1.0.0+) and additional snapshot repositories for milestones and `SNAPSHOT` builds, including Maven mirror caveats.

### 3.3. Dependency Management

Shows importing the `spring-ai-bom` so all Spring AI module versions align with a given release.

### 3.4. Add dependencies for specific components

Points to per-feature documentation for the exact starter or module coordinates each capability requires.

### 3.5. Spring AI samples

Links to community examples and sample repositories for hands-on exploration beyond the reference docs.

---

## 4. Reference

API- and integration-focused documentation for building production Spring AI applications: clients, models, memory, tools, MCP, RAG, vector stores, observability, and local dev services.

### 4.1. Chat Client

Fluent, WebClient-style API for synchronous and streaming chat: building prompts, options, structured `.entity()` responses, multiple models, and default advisors. Subtopics in the guide include Advisors and Recursive Advisors.

### 4.2. Prompts

Low-level `Prompt` / `Message` API: roles, `PromptTemplate`, resource-backed templates, convenience accessors, and prompt-engineering patterns at the foundation below `ChatClient`.

### 4.3. Structured Output

`StructuredOutputConverter` implementations (`BeanOutputConverter`, `MapOutputConverter`, `ListOutputConverter`) plus native structured output via advisors when the model supports JSON schema directly.

### 4.4. Multimodality

How `UserMessage` carries text plus `Media` (images, audio, video) for multimodal models, and which Spring AI chat integrations support it.

### 4.5. Models

Overview of the portable Model API and Boot autoconfiguration for Chat, Embedding, Image, Audio (transcription/speech), and Moderation models across major cloud and local providers (OpenAI, Anthropic, Azure, Bedrock, Google, Ollama, and others—each with a dedicated provider page in the full guide).

### 4.6. Chat Memory

Conversation persistence via `ChatMemory`, repository backends, and memory advisors (`MessageChatMemoryAdvisor`, `PromptChatMemoryAdvisor`, `VectorStoreChatMemoryAdvisor`) integrated with `ChatClient`.

### 4.7. Tool Calling

Registering `@Tool` methods and `ToolCallback` beans, tool groups, return-direct behavior, and the tool invocation loop—often used with `ToolCallAdvisor` for advisor-chain–based execution.

### 4.8. Model Context Protocol (MCP)

Standard protocol for AI apps to discover and invoke external tools and resources; Spring AI integrates the MCP Java SDK via Boot starters and annotations for both MCP clients and servers (STDIO, SSE, Streamable-HTTP). Nested docs cover client/server starters, security (WIP), and annotation-based tool/resource/prompt registration.

### 4.9. Retrieval Augmented Generation (RAG)

Modular RAG with `QuestionAnswerAdvisor`, `RetrievalAugmentationAdvisor`, query transformers/expanders, document retrievers, and the `spring-ai-rag` library’s LEGO-style pipeline building blocks. The companion **ETL Pipeline** page covers `DocumentReader` / `DocumentTransformer` / `DocumentWriter` ingestion (PDFs, splitting, vector-store loading).

### 4.10. Model Evaluation

`Evaluator` API with `RelevancyEvaluator` and `FactCheckingEvaluator` for integration tests that guard against hallucinations and irrelevant RAG answers.

### 4.11. Vector Stores

Portable `VectorStore` / `VectorStoreRetriever` API, metadata filters, similarity search, and implementations for Azure, Bedrock Knowledge Base, Cassandra, Chroma, Couchbase, Elasticsearch, GemFire, MariaDB, Milvus, MongoDB Atlas, Neo4j, OpenSearch, Oracle, PGVector, Pinecone, Qdrant, Redis, S3 Vector Store, Typesense, Weaviate, and related Boot starters.

### 4.12. Observability

Metrics and tracing for `ChatClient`, advisors, `ChatModel`, `EmbeddingModel`, `ImageModel`, vector stores, and tool calls—built on Spring’s observability stack with documented span attributes.

### 4.13. Development-time Services

Spring Boot Docker Compose support that auto-wires connection details for Ollama, Chroma, Qdrant, Weaviate, MongoDB Atlas Local, OpenSearch, Typesense, LocalStack, and MCP gateway containers during development.

### 4.14. Testcontainers

Testcontainers-based service connections for integration tests against real vector stores and model services in CI-friendly environments.

---

## 5. Guides

Task-oriented tutorials and patterns beyond the core API reference.

### 5.1. Awesome Spring AI

Community-curated [awesome-spring-ai](https://github.com/spring-ai-community/awesome-spring-ai) list of examples, libraries, and resources.

### 5.2. Getting Started with MCP

Hands-on MCP introduction: annotation-based server tools, Streamable-HTTP configuration, client `ToolCallbackProvider` wiring, and links to tutorial repos and videos.

### 5.3. Dynamic Tool Discovery

Tool Search Tool pattern via community `tool-search-tool` and `ToolSearchToolCallAdvisor`: index many tools locally, expose only a search tool to the LLM, and expand definitions on demand for large MCP/tool catalogs.

### 5.4. LLM-as-a-Judge Evaluation

Guide for using an LLM to score or judge another model’s outputs in test and quality workflows (companion to the Evaluation Testing API).

### 5.5. Prompt Engineering Patterns

Catalog of practical prompt patterns (zero/few-shot, chain-of-thought, ReAct, etc.) aligned with Spring AI’s prompt APIs.

### 5.6. Building Effective Agents

Implements Anthropic’s workflow vs. agent guidance with Spring AI examples: chain, parallelization, routing, orchestrator–workers, and evaluator–optimizer patterns from `spring-ai-examples`.

---

## 6. Upgrade Notes

Version-to-version migration guidance, breaking changes, and remediation steps when upgrading Spring AI releases.

### 6.1. Migrating FunctionCallback to ToolCallback API

Steps to move from the older function-callback API to the current `ToolCallback` / `@Tool` tool-calling model.

### 6.2. Migrating the Anthropic Module to the Official Java SDK

Migration notes for the Anthropic integration’s move to the official Anthropic Java SDK and related configuration changes.

---

*Document structure mirrors [EMBABEL_AGENT_GUIDE_TOPICS.md](EMBABEL_AGENT_GUIDE_TOPICS.md). Nav source: Spring AI `nav.adoc` (main branch). See also [SPRING_AI_GUIDE_COVERAGE.md](SPRING_AI_GUIDE_COVERAGE.md) · [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md).*
