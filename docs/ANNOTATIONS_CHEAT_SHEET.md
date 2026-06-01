# Spring AI & Embabel annotations cheat sheet

Quick reference for **AI-related annotations** used in Embabel agent apps on Spring Boot. This repo pins **Embabel 0.4.0** (which pulls **Spring AI 1.1.5** transitively). The [Embabel guide 0.5.0-SNAPSHOT](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) documents additional APIs (called out below).

**Related docs in this repo:** [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md) · [EMBABEL_AGENT_GUIDE_TOPICS.md](EMBABEL_AGENT_GUIDE_TOPICS.md) · [SPRING_AI_REFERENCE_TOPICS.md](SPRING_AI_REFERENCE_TOPICS.md) · [GUIDE_COVERAGE.md](GUIDE_COVERAGE.md)

---

## How the layers relate

| Layer | Package(s) | Role |
|-------|------------|------|
| **Embabel agent model** | `com.embabel.agent.api.annotation.*` | Agents, GOAP/utility planning, goals, domain tools |
| **Spring AI tool calling** | `org.springframework.ai.tool.annotation.*` | `@Tool` methods for `ChatClient` / `ChatModel` tool loops |
| **Spring AI MCP** | `org.springaicommunity.mcp.annotation.*` | Declarative MCP server tools/resources/prompts and client handlers (integrated via `spring-ai-mcp-annotations`) |

Embabel actions typically use **`@LlmTool`** (or Spring `@Tool`) on domain objects and **`PromptRunner.withToolGroup(...)`** for MCP — not `@Tool` on `@Agent` classes. See [EMBABEL_AND_SPRING_AI.md](EMBABEL_AND_SPRING_AI.md).

---

## At-a-glance

### Embabel (`com.embabel.agent.api.annotation`)

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@Agent` | Class | Register an agent (Spring bean + planner participant) |
| `@EmbabelComponent` | Class | Expose actions/goals without being a full agent (e.g. chat router) |
| `@Action` | Method | Planner step / capability |
| `@AchievesGoal` | Method | Terminal step that satisfies the agent goal |
| `@Condition` | Method | Boolean precondition for planning |
| `@Cost` | Method | Dynamic cost/value for utility planning |
| `@State` | Class | State-machine type for phased / looping workflows |
| `@Export` | Method (or nested in `@AchievesGoal`) | Expose goal as local/remote (MCP) tool |
| `@LlmTool` | Method | In-process LLM tool on a domain object |
| `@LlmTool.Param` | Parameter | Tool parameter metadata |
| `@LlmTool.Meta` | Method | Key/value tool metadata |
| `@Provided` | Parameter | Inject from Spring/`OperationContext`, not blackboard |
| `@RequireNameMatch` | Parameter | Bind blackboard object by type **and** name |
| `@UnfoldingTools` | Class | Progressive disclosure (“matryoshka”) tool facade |
| `@MatryoshkaTools` | Class | Same shape as `@UnfoldingTools` (alternate name in API) |
| `@ToolGroup` | Type | Tool-group role metadata |
| `@AgentCapabilities` | Type | Capability scanning marker |

**Not annotations (but related):** `RunSubagent` (utility for nested agent runs), `SpecialReturnException` (control flow), `WaitFor` (human-in-the-loop API on states — not an annotation).

### Spring AI — tool calling (`org.springframework.ai.tool.annotation`)

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@Tool` | Method | Expose method as LLM tool (`ChatClient.tools(...)`, `ToolCallbacks.from(...)`) |
| `@ToolParam` | Parameter | Parameter description / required flag for JSON schema |

### Spring AI — MCP (`org.springaicommunity.mcp.annotation`)

Documented under [Spring AI MCP annotations](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-overview.html); types live in the **`mcp-annotations`** artifact (Spring AI 1.1.x).

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@McpTool` | Method (server) | MCP tool with auto schema |
| `@McpToolParam` | Parameter (server) | MCP tool argument metadata |
| `@McpResource` | Method (server) | MCP resource (URI template) |
| `@McpPrompt` | Method (server) | MCP prompt template |
| `@McpComplete` | Method (server) | MCP completion / autocomplete |
| `@McpArg` | Parameter | MCP prompt argument metadata |
| `@McpLogging` | Method (client) | Handle server log notifications |
| `@McpSampling` | Method (client) | Handle server sampling requests |
| `@McpElicitation` | Method (client) | Handle server elicitation requests |
| `@McpProgress` | Method (client) | Handle progress notifications |
| `@McpToolListChanged` | Method (client) | Tool list changed |
| `@McpResourceListChanged` | Method (client) | Resource list changed |
| `@McpPromptListChanged` | Method (client) | Prompt list changed |
| `@McpProgressToken` | Parameter | Injected progress token (excluded from schema) |

**Special parameter types (not annotations):** `McpSyncRequestContext`, `McpAsyncRequestContext`, `McpTransportContext`, `McpMeta`.

---

## Embabel reference

Official: [Guide §4.6 Annotation model](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) (wording may differ slightly by version).

### `@Agent`

**Target:** class · **Stereotype:** Spring component + registered agent.

| Attribute | Type | Default | Notes |
|-----------|------|---------|-------|
| `description` | `String` | — | **Required** — used for autonomy / agent selection |
| `name` | `String` | `""` | Display name |
| `provider` | `String` | `""` | Provider id |
| `version` | `String` | `""` | Version label |
| `planner` | `PlannerType` | enum default | e.g. GOAP vs UTILITY |
| `scan` | `boolean` | `true` | Component scan |
| `beanName` | `String` | `""` | Spring bean name |
| `opaque` | `boolean` | `false` | Hide internals from selection |
| `actionRetryPolicy` | `ActionRetryPolicy` | — | Retry policy enum |
| `actionRetryPolicyExpression` | `String` | `""` | SpEL/expression for retry |

```java
@Agent(description = "Inspect git changes and suggest a commit message")
public class CommitMessageAgent { ... }
```

### `@EmbabelComponent`

**Target:** class · Actions/goals usable by planners (especially **utility** chat routing), but **not** a standalone agent.

| Attribute | Type | Default |
|-----------|------|---------|
| `scan` | `boolean` | `true` |

```java
@EmbabelComponent
public class ChatRouter {
  @Action(canRerun = true, trigger = UserMessage.class)
  public void respond(Conversation conversation, ActionContext context) { ... }
}
```

### `@Action`

**Target:** method · **Requires:** ≥1 parameter (except `@Condition`).

| Attribute | Type | Default | Notes |
|-----------|------|---------|-------|
| `description` | `String` | `""` | Human + LLM-readable |
| `pre` | `String[]` | `{}` | Preconditions; use `spel:...` for SpEL on blackboard |
| `post` | `String[]` | `{}` | Postconditions |
| `canRerun` | `boolean` | `false` | Allow re-execution |
| `readOnly` | `boolean` | `false` | No external side effects |
| `clearBlackboard` | `boolean` | `false` | Keep only this action’s output on blackboard |
| `outputBinding` | `String` | `""` | Name for output on blackboard |
| `cost` | `double` | `0.0` | Relative cost 0–1 (utility planner) |
| `value` | `double` | `0.0` | Relative value 0–1 |
| `costMethod` | `String` | `""` | Name of `@Cost` method |
| `valueMethod` | `String` | `""` | Name of `@Cost` method for value |
| `trigger` | `Class<?>` | `Void.class` | Fire only when this type was **last** added to blackboard |
| `actionRetryPolicy` | `ActionRetryPolicy` | — | Per-action retry |
| `actionRetryPolicyExpression` | `String` | `""` | Expression for retry |

**Common injected parameter types:** `Ai`, `ActionContext` / `OperationContext`, `UserInput`, domain types from blackboard.

```java
@Action(canRerun = true, description = "Suggest a commit message from current git changes")
public CommitMessage answer(Request request, ActionContext context) { ... }

@AchievesGoal(description = "Propose a commit message for the current changes")
@Action
@Export(remote = true)
public CommitMessage generateCommitMessage(GitChanges changes, UserInput userInput, Ai ai) { ... }
```

### `@AchievesGoal`

**Target:** method (with `@Action`) · Marks goal completion.

| Attribute | Type | Default |
|-----------|------|---------|
| `description` | `String` | `""` |
| `value` | `double` | `0.0` |
| `tags` | `String[]` | `{}` |
| `examples` | `String[]` | `{}` |
| `export` | `@Export` | default `Export` |

Can nest export: `@AchievesGoal(..., export = @Export(remote = true, name = "myGoal"))`.

### `@Condition`

**Target:** method · **No side effects** — may run repeatedly.

| Attribute | Type | Default |
|-----------|------|---------|
| `name` | `String` | `""` |
| `cost` | `double` | `0.0` |

Returns `boolean`. May take `OperationContext` and/or domain parameters (false until types exist on blackboard).

### `@Cost`

**Target:** method · Returns `double` 0.0–1.0 for dynamic planning.

| Attribute | Type | Default |
|-----------|------|---------|
| `name` | `String` | `""` | Referenced from `@Action(costMethod=...)` / `valueMethod=...` |

Domain parameters must be **nullable** — missing blackboard objects are passed as `null`.

### `@State`

**Target:** class (often `record`) · Enables state-machine / loop workflows; actions can live **on** the state type.

Inherited by subclasses/implementations. Pair with `@Action(clearBlackboard = true)` for loops. See guide §4.19 / §4.18.5.

### `@Export`

**Target:** method or nested in `@AchievesGoal` · Publish goal as invocable tool (including MCP when `remote = true`).

| Attribute | Type | Default |
|-----------|------|---------|
| `name` | `String` | `""` |
| `remote` | `boolean` | `false` | Expose via MCP server (Embabel mcpserver starter) |
| `local` | `boolean` | `true` | In-process export |
| `startingInputTypes` | `Class<?>[]` | `{}` | MCP prompt / tool input shapes |

Used in this repo on `CommitMessageAgent.generateCommitMessage` and `CommitStyleAgent` ([TUTORIAL-MCP.md](../TUTORIAL-MCP.md)).

### `@LlmTool` / `@LlmTool.Param` / `@LlmTool.Meta`

**Target:** method (class need not be annotated) · In-process tools for `PromptRunner` / tool loop.

| `@LlmTool` attribute | Type | Default |
|---------------------|------|---------|
| `description` | `String` | `""` |
| `name` | `String` | `""` |
| `returnDirect` | `boolean` | `false` |
| `category` | `String` | `""` | Used with `@UnfoldingTools` |
| `metadata` | `LlmTool.Meta[]` | `{}` |

| `@LlmTool.Param` | Type | Default |
|------------------|------|---------|
| `description` | `String` | `""` |
| `required` | `boolean` | `true` |

Embabel also accepts Spring AI **`@Tool`** on the same tool objects ([guide §4.9](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)).

```java
@LlmTool(description = "Returns the name of the current git branch")
public String currentBranch() { ... }
```

### `@Provided`

**Target:** parameter · Resolve from Spring / platform context (not blackboard). Essential inside `@State` types to reach enclosing services.

### `@RequireNameMatch`

**Target:** parameter · Bind by **type + parameter name** on blackboard. Use with `@Action(outputBinding = "thingOne")`.

### `@UnfoldingTools` / `@MatryoshkaTools`

**Target:** class · Expose a facade tool; LLM picks a category to unfold child `@LlmTool` methods.

| Attribute | Type | Default |
|-----------|------|---------|
| `name` | `String` | required |
| `description` | `String` | required |
| `removeOnInvoke` | `boolean` | `true` (deprecated in 0.5 docs) |
| `categoryParameter` | `String` | `"category"` |
| `childToolUsageNotes` | `String` | `""` |

### `@ToolGroup` / `@AgentCapabilities`

| Annotation | Attribute | Purpose |
|------------|-----------|---------|
| `@ToolGroup` | `role` | Metadata for tool-group registration |
| `@AgentCapabilities` | `scan` | Mark capability types for scanning |

### Embabel 0.5+ (not in 0.4.0 JAR)

| Annotation | Purpose |
|------------|---------|
| `@SecureAgentTool("SpEL")` | Spring Security SpEL on agent class or `@Action` before remote MCP tool execution ([TUTORIAL-SECURE-TOOLS.md](../TUTORIAL-SECURE-TOOLS.md)) |

Guide §4.6 also documents **`toolGroups` / `toolGroupRequirements`** on `@Action` in newer releases — check your Embabel version’s `Action` interface if you upgrade past 0.4.0.

---

## Spring AI — `@Tool` / `@ToolParam`

Official: [Tool calling](https://docs.spring.io/spring-ai/reference/api/tools.html)

| `@Tool` attribute | Type | Default |
|-------------------|------|---------|
| `name` | `String` | method name |
| `description` | `String` | `""` |
| `returnDirect` | `boolean` | `false` | Skip second model turn; return tool output to client |
| `resultConverter` | `Class<? extends ToolCallResultConverter>` | default converter |

| `@ToolParam` attribute | Type | Default |
|------------------------|------|---------|
| `description` | `String` | `""` |
| `required` | `boolean` | `true` |

**Usage:** pass tool object instances to `ChatClient.create(model).prompt(...).tools(new MyTools()).call()` or register `ToolCallback` beans.

**Schema extras:** `@Nullable` (optional param), Swagger `@Schema`, Jackson `@JsonProperty`.

**Not supported as tool param/return types:** `Optional`, `Future`/`CompletableFuture`, Reactor types, functional interfaces.

---

## Spring AI — MCP annotations

Official: [MCP annotations overview](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-overview.html) · [Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-server.html) · [Client](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-client.html)

**Package:** `org.springaicommunity.mcp.annotation` (artifact `org.springaicommunity:mcp-annotations`, pulled transitively by Spring AI MCP starters).

Enable scanning (Boot):

```yaml
spring.ai.mcp.server.annotation-scanner.enabled: true
spring.ai.mcp.client.annotation-scanner.enabled: true
```

Client handlers **must** set `clients = "connectionName"` matching `spring.ai.mcp.client.*.connections.<name>`.

### Server

#### `@McpTool`

| Attribute | Type | Notes |
|-----------|------|-------|
| `name` | `String` | Tool id for MCP |
| `description` | `String` | |
| `annotations` | `McpTool.McpAnnotations` | `title`, `readOnlyHint`, `destructiveHint`, `idempotentHint`, … |
| `generateOutputSchema` | `boolean` | |
| `title` | `String` | |

#### `@McpToolParam`

| Attribute | Type | Default |
|-----------|------|---------|
| `description` | `String` | `""` |
| `required` | `boolean` | `true` |

#### `@McpResource`

| Attribute | Type |
|-----------|------|
| `uri` | `String` | URI template, e.g. `config://{key}` |
| `name` | `String` |
| `title` | `String` |
| `description` | `String` |
| `mimeType` | `String` |

#### `@McpPrompt`

| Attribute | Type |
|-----------|------|
| `name` | `String` |
| `title` | `String` |
| `description` | `String` |

#### `@McpComplete`

| Attribute | Type |
|-----------|------|
| `prompt` | `String` | Prompt name for completion |
| `uri` | `String` | Alternative URI-based completion |

#### `@McpArg` (prompt parameters)

| Attribute | Type | Default |
|-----------|------|---------|
| `name` | `String` | |
| `description` | `String` | `""` |
| `required` | `boolean` | `true` |

### Client (all require `clients = { "..." }`)

| Annotation | Handler receives |
|------------|------------------|
| `@McpLogging` | `LoggingMessageNotification` or discrete params |
| `@McpSampling` | `CreateMessageRequest` → `CreateMessageResult` |
| `@McpElicitation` | `ElicitRequest` → `ElicitResult` |
| `@McpProgress` | `ProgressNotification` or discrete params |
| `@McpToolListChanged` | `List<McpSchema.Tool>` |
| `@McpResourceListChanged` | `List<McpSchema.Resource>` |
| `@McpPromptListChanged` | `List<McpSchema.Prompt>` |

### Parameter markers

| Marker | Role |
|--------|------|
| `@McpProgressToken` | Injects progress token; excluded from JSON schema |
| `McpMeta` | Request metadata map (injected; not an annotation) |

**Sync vs async:** SYNC servers accept non-reactive return types; ASYNC servers expect `Mono`/`Flux`. Stateful servers allow `McpSyncRequestContext` / `McpAsyncRequestContext`; stateless servers filter those out ([server doc](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-server.html)).

---

## Embabel vs Spring AI — which tool annotation?

| Use case | Prefer |
|----------|--------|
| GOAP / `@Action` agent step with domain object | `@LlmTool` on domain type + `withToolObject(...)` / tool groups |
| Plain `ChatClient` without Embabel process | `@Tool` + `ToolCallbacks` |
| Expose **your** JVM app over MCP wire | `@McpTool` (Spring AI) **or** `@Export(remote=true)` on Embabel goals |
| Consume external MCP server in Embabel agent | Spring AI MCP **client** autoconfig + Embabel `McpToolGroup` (no `@McpTool` on your code) |

---

## Examples in simple-demo

| File | Annotations |
|------|-------------|
| `agent/*Agent.java` | `@Agent`, `@Action`, `@AchievesGoal`, `@Export` |
| `chat/ChatRouter.java` | `@EmbabelComponent`, `@Action(trigger=UserMessage.class)` |
| `git/GitRepository.java` | `@LlmTool` |
| MCP publish | `@Export(remote = true)` on goal methods — [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) |

---

## Version notes

| Component | This repo | Cheat sheet source |
|-----------|-----------|-------------------|
| Embabel | **0.4.0** | `embabel-agent-api` JAR + [0.5 guide](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) for forward-looking items |
| Spring AI | **1.1.5** (via Embabel BOM) | [Spring AI reference](https://docs.spring.io/spring-ai/reference/index.html) |
| MCP annotations | **0.8.0** (`mcp-annotations`) | Spring AI MCP docs |

When upgrading Embabel to **0.5.x**, re-scan `com.embabel.agent.api.annotation` for new attributes (`@SecureAgentTool`, expanded `@Action` tool group fields, etc.).

---

## Further reading

- Embabel: [Annotation model (§4.6)](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) · [Tools (§4.9)](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) · [States (§4.19)](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/)
- Spring AI: [Tool calling](https://docs.spring.io/spring-ai/reference/api/tools.html) · [MCP overview](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
- Repo tutorials: [TUTORIAL-TOOLS.md](../TUTORIAL-TOOLS.md) · [TUTORIAL-MCP.md](../TUTORIAL-MCP.md) · [TUTORIAL-ROUTER.md](../TUTORIAL-ROUTER.md)
