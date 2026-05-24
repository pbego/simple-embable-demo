# Embabel Agent Framework User Guide — Topics (0.5.0-SNAPSHOT)

Source: [Embabel Agent Framework User Guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)

Topics are listed up to the second heading level (`x` and `x.x`). Each entry has a short summary (one or two sentences).

**Coverage in this repo:** [GUIDE_COVERAGE.md](GUIDE_COVERAGE.md) — what simple-demo already demonstrates vs gaps.

---

## 1. Overview

Introduces agentic AI on the JVM and positions Embabel as a framework for embedding goal-driven, tool-using agents into existing enterprise applications without replacing core systems.

### 1.1. Glossary

Defines core vocabulary—Agent, Tools, MCP, and DICE (Domain Integrated Context Engineering)—for readers new to applied agentic development.

### 1.2. Why do we need an Agent Framework?

Explains why raw LLMs and MCP alone are insufficient for business apps, citing explainability, discoverability, model mixing, guardrails, resilience, composability, and safer integration with sensitive systems.

### 1.3. Embabel Differentiators

Summarizes what sets Embabel apart: GOAP-based planning, extensibility without FSM edits, strong typing, platform abstraction, LLM mixing, Spring/JVM integration, and testability.

### 1.4. Core Concepts

Describes how Embabel models flows with Actions, Goals, Conditions, and a domain model, and how dynamic GOAP replanning forms an OODA loop driven by typed data flow.

---

## 2. Getting Started

Walks through bootstrapping a project, obtaining dependencies, configuring LLM providers, and running your first agents.

### 2.1. Quickstart

Points to Java/Kotlin GitHub templates and the `project-creator` tool so you can scaffold a working Embabel project quickly.

### 2.2. Getting the Binaries

Covers Maven/Gradle starters (shell, MCP server, basic platform), snapshot repositories, and environment setup for OpenAI, Anthropic, DeepSeek, Gemini, Mistral, LM Studio, Ollama, and related providers.

### 2.3. Getting Embabel Running

Explains how to run example projects, prerequisites, shell usage, example commands, and implementing custom shell commands.

### 2.4. Adding a Little AI to Your Application

Shows minimal AI integration by injecting `Ai` or `OperationContext` into Spring components for text generation and structured `createObject()` calls without defining a full agent.

### 2.5. Writing Your First Agent

Uses the template `WriteAndReviewAgent` to introduce `@Agent`, `@Action`, multi-step flows, and how to run agents from the shell.

---

## 3. Embabel Shell

Documents the Spring Shell–based CLI for running, debugging, and iterating on agents interactively.

### 3.1. How to Use the Shell

Covers starting the shell, navigation, how `UserInput` reaches agents via the blackboard, and logging verbosity flags (`-p`, `-r`).

### 3.2. Shell Commands

Reference for `execute`/`x`, `chat`, `choose-goal`, tool-call context commands (`set-context`, `show-context`), and custom command registration.

### 3.3. Embabel Modules

Catalog of framework modules (core, features, RAG, starters, test support, examples) and notes on experimental APIs.

---

## 4. Reference

Comprehensive API and behavior reference for building, configuring, invoking, and operating production Embabel agents.

### 4.1. Invoking an Agent

Describes programmatic vs user-input invocation and how closed vs open autonomy modes select agents or assemble actions toward a goal.

### 4.2. Agent Process Flow

Details `AgentProcess` lifecycle states, GOAP replanning after each action, the blackboard pattern, binding by type/name, and cross-process `Context`.

### 4.3. Goals, Actions and Conditions

Section anchor for the planning primitives; goals, actions, and conditions are defined via annotations and inferred from method signatures throughout the guide.

### 4.4. Domain Objects

Explains rich domain objects with `@Tool` behavior, selective LLM exposure, use in actions, DICE principles, and benefits for context and testability.

### 4.5. Configuration

Documents enabling Embabel, `application.yml` properties (LLMs, platform, logging, scanning, ranking, tool loop, autonomy, providers, HTTP, SSE, REST, tests), and advanced provider setup.

### 4.6. Annotation model

Reference for `@Agent`, `@EmbabelComponent`, `@Action`, `@Condition`, `@AchievesGoal`, parameters, binding, triggers, return types, security, subagents, and exception handling.

### 4.7. DSL

Describes programmatic agent builders (`SimpleAgentBuilder`, `ScatterGatherBuilder`, `ConsensusBuilder`, `RepeatUntil`, etc.) and registering DSL-built agents as Spring beans.

### 4.8. Core Types

Covers `LlmOptions`, `PromptRunner` (obtaining instances and methods), and `AgentImage` for multimodal inputs.

### 4.9. Tools

Explains in-process `@LlmTool` tools, tool groups, MCP consumption, out-of-band `ToolCallContext`, framework-agnostic `Tool` interface, and unfolding tool patterns.

### 4.10. Structured Prompt Elements

Documents `PromptContributor`, `LlmReference`, built-in helpers (`Persona`, `RoleGoalBackstory`), custom contributors, and prompt best practices.

### 4.11. Templates

Describes Jinja-based prompts via `PromptRunner.rendering()` and optional custom `TemplateRenderer` for per-tenant or filesystem-backed templates.

### 4.12. RAG (Retrieval-Augmented Generation)

Covers agentic, tool-driven RAG with `ToolishRag`, store facades, ingestion, vector/text search, HyDE, and integration with `LlmReference` and chatbots.

### 4.13. Building Chatbots

Guides explicit `Conversation`/`Chatbot` design with long-lived `AgentProcess`, utility AI for message handling, and patterns distinct from Embabel’s non-threaded core execution.

### 4.14. The AgentProcess

Brief definition: each agent run creates an `AgentProcess` with a unique identifier for tracking and lifecycle management.

### 4.15. Execution Modes

Compares default sequential `SimpleAgentProcess` with parallel `ConcurrentAgentProcess`, activation via `process-type`, and replanning behavior under concurrency.

### 4.16. ProcessOptions

Lists runtime options: `contextId`, initial blackboard, test mode, verbosity, termination policies, delays, ephemeral processes, and `toolCallContext` for tools/MCP.

### 4.17. The AgentPlatform

Introduces the SPI `AgentPlatform` as the environment abstraction for creating and running agent processes.

### 4.18. Invoking Embabel Agents

Shows strongly typed programmatic invocation via `AgentPlatform`, bindings, synchronous/async execution, REST, webhooks, and autonomy APIs.

### 4.19. Using States

Explains `@State` for loops and phased workflows within GOAP, including blackboard clearing, goals on terminal states, and human-in-the-loop `WaitFor`.

### 4.20. Choosing a Planner

Compares GOAP (default), Utility, Hybrid, and Supervisor planners with guidance on when to use each and nested workflow patterns.

### 4.21. API vs SPI

Clarifies that application code should depend on `com.embabel.agent.api.*` only; SPI packages are for extenders and may change without notice.

### 4.22. Embabel and Spring

Describes Embabel’s foundation on Spring and Spring AI, dependency injection, AOP, and why Spring Boot suits production agentic applications on the JVM.

### 4.23. Working with LLMs

Covers model selection per action, tuning for smaller/local models, custom `LlmMessageSender`, tool-loop policies, BYOK, embeddings, and parallel tool execution.

### 4.24. AWS Bedrock Integration

Setup for Bedrock models via the autoconfiguration starter, credentials, and configuration properties.

### 4.25. MiniMax Integration

Configuration for MiniMax’s OpenAI-compatible API as a first-class Embabel provider.

### 4.26. Working with Streams

Documents streaming LLM output, reasoning/thinking events, and structured object streams via `StreamingEvent`.

### 4.27. Working with LLM Reasoning / Thinking

Explains capturing and validating model reasoning chains alongside structured results for audit and correctness checks.

### 4.28. Working with Callbacks (Interceptors)

Describes tool-loop inspectors and transformers for observing or modifying LLM calls, tool execution, and loop lifecycle.

### 4.29. Tracking LLM Cost and Usage

Covers `LlmInvocationEvent` and embedding events for real-time cost, model, and process attribution.

### 4.30. Working with Guardrails

Framework for input/output validation policies via `withGuardRails`, custom POJO or Spring bean guardrails, and integration patterns.

### 4.31. Agent and Action Termination

Mechanisms for graceful (signal) vs immediate (exception) termination for cancellation, timeouts, budgets, and critical failures.

### 4.32. Customizing Embabel

Extending Embabel with custom `LlmService`/`SpringAiLlmService`, planners, platforms, and other SPI hooks.

### 4.33. Integrations

MCP publishing and consumption, external tool servers, and related system-integration patterns.

### 4.34. Developer Tooling

Placeholder section grouping IDE and developer-experience topics; see IntelliJ plugin and Agent Skills subsections.

### 4.35. IntelliJ IDEA Plugin

Suppresses false “unused method” warnings for `@Action`, `@Condition`, and `@Cost` via implicit-usage support in the IDE.

### 4.36. Agent Skills

Implements the [Agent Skills Specification](https://agentskills.io/specification) for reusable skill packages (`SKILL.md`, resources, tools) loaded into agents.

### 4.37. Testing

Unit and integration testing support, test doubles, process execution in tests, and quality practices for agentic applications.

### 4.38. Embabel Architecture

Pointers to architectural diagrams and concepts distributed across planner, platform, and process documentation.

### 4.39. Troubleshooting

Common problems (versions, planning failures, LLM errors, binding issues) and practical fixes.

### 4.40. Migrating from other frameworks

Guidance for moving from Python-centric frameworks (e.g. CrewAI) to Embabel’s typed, JVM-integrated model.

### 4.41. API Evolution

Stability commitments for core APIs (`Ai`, `PromptRunner`), experimental/internal markers, and SPI non-guarantees.

---

## 5. Asynchronous Mode and Java 25

Describes async execution via `Asyncer`, virtual threads, and Java 25 cgroup CPU detection effects on parallelism.

### 5.1. Java 25 Implications

Explains why container CPU limits can serialize `ForkJoinPool.commonPool` on Java 25, why Embabel core routes through Spring executors safely, and optional workarounds.

---

## 6. Design Considerations

Guidance on balancing LLM autonomy with deterministic code control when designing agent systems.

### 6.1. Domain objects

Advocates rich (non-anemic) domain models with `@Tool` exposure, persistence options, and roles in typing, code, and LLM toolability.

### 6.2. Tool Call Choice

Discusses when to use MCP or external tools versus in-agent method calls and domain-bound tools.

### 6.3. Mixing LLMs

Recommends using different models per action for cost, capability, and environmental fit.

---

## 7. Contributing

How to contribute code, docs, bugs, and community engagement; expectations for PR quality and understanding your changes.

---

## 8. Resources

Curated external reading: blog posts, tutorials, example repos, Tripper demo, GOAP/OODA background, and DDD references.

### 8.1. Rod Johnson’s Blog Posts

Links to introductory, vision, and DICE/context-engineering articles by Rod Johnson.

### 8.2. Examples and Tutorials

Third-party tutorials plus the examples repo, Java template, and Kotlin template.

### 8.3. Sophisticated Example: Tripper Travel Planner

Overview of the Tripper project as a production-style travel-planning agent with multi-model use and deployment patterns.

### 8.4. Goal-Oriented Action Planning (GOAP)

Introductory GOAP material, NVIDIA “code vs LLM agency” research, and the OODA loop.

### 8.5. Domain-Driven Design

DDD primers and advanced validation concepts aligned with Embabel’s domain modeling approach.

---

## 9. APPENDIX

Appendix section in the published guide (reserved for supplementary material).

---

## 10. Planning Module

Lower-level planning module documentation used by the Embabel Agent Platform.

### 10.1. Abstract

Brief description of the planning/scheduling module as infrastructure beneath the agent platform.

### 10.2. A* GOAP Planner Algorithm Overview

Technical overview of the A* GOAP implementation: search components, forward/backward optimization, pruning, and process flow.

### 10.3. Agent Pruning Process

Documents how irrelevant actions are pruned from plans to keep execution efficient.
