# Chat history and memory

This guide explains how **simple-demo** saves conversations to disk, how the LLM uses prior messages as **memory**, and which shell commands to use.

For the git commit message agent itself, see [TUTORIAL.md](TUTORIAL.md).

## Concepts

| Term | Meaning in simple-demo |
|------|-------------------------|
| **Chat history** | All messages stored in a conversation file on disk |
| **Memory (for the LLM)** | The last N messages from that history sent on each turn (`simple-demo.memory-max-messages`, default 20) |
| **Conversation** | Embabel’s `Conversation` type — explicit message list, not Spring AI `ChatMemory` |
| **Conversation id** | Short id (filename stem), e.g. `a1b2c3d4` — use with `resume-chat` |

**File profile (default):** JSON under `~/.simple-demo/conversations/<id>.json` (override `simple-demo.conversations-dir` in `application-file.properties`).

**Postgres profile:** rows in `conversations` and `messages` (see [TUTORIAL-AUDIT.md](TUTORIAL-AUDIT.md)). Start with `docker compose up -d` and `-Dspring.profiles.active=postgres`.

History **survives** shell `exit` and app restarts. It is **not** shared across machines unless you copy files or use a shared database.

## Message windowing

**Chat history** (on disk) and **LLM context** (what the model sees on each turn) are not the same:

- **Saved:** every user and assistant message is written to the JSON file.
- **Sent to the LLM:** only the last **N** messages from that file, plus a system prompt (default **N = 20** via `simple-demo.memory-max-messages`).

`ChatRouter` uses `ChatContextService` and `ChatPromptBuilder`: a rolling `session_summary` plus `conversation.last(n)` for LLM calls. Replies are still appended to the **full** conversation and persisted. Older messages remain in storage but drop out of the model’s context once the thread is longer than N turns (unless folded into the summary).

This is an application-level **message-count** cap. It is related to, but not the same as, a model’s **token** context window (e.g. 128k): if those N messages are very long, the provider can still reject or truncate the request. Raise N in `application.properties` if you need more recent turns in context, or use summarization/RAG for much older content.

## Quick start (two days)

**Day 1 — new chat**

```text
shell:> chat
You: What changed in my repo?
You: Suggest a conventional commit for the staged files
You: exit
```

Embabel’s built-in `chat` command uses our `Chatbot` bean. Each message is saved automatically to `~/.simple-demo/conversations/<id>.json`. Note the conversation id from the startup line (or list later).

**Day 2 — list and resume**

```text
shell:> conversations

ID         UPDATED           PREVIEW
a1b2c3d4   2026-05-20 10:15  Suggest a conventional commit for the staged files

shell:> resume-chat a1b2c3d4
You: Make the subject shorter
You: exit
```

The model sees the last N messages from that file (see [Message windowing](#message-windowing)).

## Shell commands

| Command | Purpose |
|---------|---------|
| `chat` | Start a **new** interactive session (Embabel built-in; persists via file store) |
| `conversations` (alias `conv-list`) | List saved conversations |
| `resume-chat <id>` (alias `chat-resume`) | Continue a saved conversation |
| `x "..."` | One-shot **structured** commit JSON (`CommitMessageAgent`) — separate from chat history |
| `clear` | Clear shell blackboard (does not delete saved JSON files) |

### Workflow options

**1. Persistent chat (recommended for multi-day work)**

- Use `chat` for new threads, `conversations` + `resume-chat` to continue.
- Free-form assistant replies; good for exploring diffs and wording.

**2. One-shot commit JSON**

- Use `x "generate a commit message for my changes"`.
- No conversation file required; output is JSON `subject` / `body`.

**3. Same-session blackboard (`x -s`)**

- Re-runs an agent with the shell blackboard from the previous `x` in the **same** JVM session.
- Different from file-backed chat history; lost after restart unless you use `chat` / `resume-chat`.

## Configuration

```properties
simple-demo.conversations-dir=${user.home}/.simple-demo/conversations
simple-demo.memory-max-messages=20
```

| Property | Default | Role |
|----------|---------|------|
| `simple-demo.conversations-dir` | `~/.simple-demo/conversations` | Where JSON files are written |
| `simple-demo.memory-max-messages` | `20` | How many recent messages are included in LLM context per turn (full history still saved) |

## How it works internally

```mermaid
flowchart LR
  chatCmd["chat / resume-chat"]
  Chatbot["AgentProcessChatbot"]
  Factory["ConversationFactory"]
  Store["ConversationStore"]
  Router["ChatRouter"]
  Ctx["ChatContextService"]

  chatCmd --> Chatbot
  Chatbot --> Factory
  Factory --> Store
  Chatbot --> Router
  Router --> Ctx
  Ctx -->|"summary + last N"| LLM[Ollama]
```

1. **`FileConversationFactory`** or **`PostgresConversationFactory`** — `load(id)` / `create(id)` via `ConversationStore`.
2. **`PersistingConversation`** — saves after every `addMessage`.
3. **`ChatRouter`** — routes each user turn to specialist `@Agent`s; **`SessionSummaryService`** may update `session_summary`.
4. **`DemoChatConfiguration`** — registers `AgentProcessChatbot` with utility planning (includes `ChatRouter`).
5. **`ChatContextService`** — builds prompts with summary + `conversation.last(memoryMaxMessages)`.

## Embabel guide mapping

- [Building Chatbots (§4.13)](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) — `Conversation`, `Chatbot`, `conversationId` on `createSession`.
- Production Embabel apps may use **`embabel-chat-store`** (Neo4j) with the same `ConversationFactory` contract. This demo uses **PostgreSQL** (`postgres` profile) or **file JSON** (`file` profile).

## Spring AI comparison

| Spring AI | simple-demo |
|-----------|-------------|
| `InMemoryChatMemoryRepository` | JSON files or PostgreSQL `messages` table |
| `MessageWindowChatMemory` | Full history on disk; LLM prompt uses last N messages (`memory-max-messages`) |
| `conversationId` | Conversation id + `resume-chat` |

## Troubleshooting

| Problem | What to check |
|---------|----------------|
| Chat stuck after you type a message | Ensure `ChatRouter` is deployed as a utility `@EmbabelComponent` and `AgentProcessChatbot.utilityFromPlatform` is used |
| Postgres connection refused | Run `docker compose up -d`; use `postgres` profile |
| Empty `conversations` list | Run `chat` at least once; verify `simple-demo.conversations-dir` exists and is writable |
| `resume-chat` says not found | Typo in id; run `conversations` for exact ids |
| Corrupt JSON | Delete the broken `*.json` file or fix JSON manually |
| No LLM reply | Ollama running at `spring.ai.ollama.base-url`; model pulled (`gemma4:e4b`) |
| `InvalidLlmReturnFormatException` on `x` (rankings JSON) | Harmless retry noise from Ollama/gemma; agent selection usually succeeds on the second attempt |

## FAQ

**Is history lost when I exit the shell?**  
No. Messages are saved after each turn. Start the app again and use `resume-chat <id>`.

**How do I start fresh?**  
Run `chat` (new session, new id). Old files remain until you delete them from the conversations directory.

**Does `x` use chat history?**  
Not by default. `x` targets `CommitMessageAgent` for structured JSON. Use `chat` / `resume-chat` for conversational memory.

**Can I delete a conversation?**  
Remove the file `~/.simple-demo/conversations/<id>.json`.
