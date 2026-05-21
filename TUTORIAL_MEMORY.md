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

Files are stored under:

```text
~/.simple-demo/conversations/<id>.json
```

Override with `simple-demo.conversations-dir` in `application.properties`.

History **survives** shell `exit` and app restarts. It is **not** shared across machines unless you copy that directory.

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

The model sees earlier user/assistant messages from the file.

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
| `simple-demo.memory-max-messages` | `20` | Reserved for future windowing; chat agent uses full saved history via Embabel |

## How it works internally

```mermaid
flowchart LR
  chatCmd["chat / resume-chat"]
  Chatbot["Chatbot bean"]
  Factory["FileConversationFactory"]
  Store["FileConversationStore"]
  Disk["*.json on disk"]
  Agent["CommitChatAgent"]

  chatCmd --> Chatbot
  Chatbot --> Factory
  Factory --> Store
  Store --> Disk
  Chatbot --> Agent
  Agent -->|"respondWithSystemPrompt(conversation)"| LLM[Ollama]
```

1. **`FileConversationFactory`** — `load(id)` reads JSON; `create(id)` wraps an in-memory conversation.
2. **`PersistingConversation`** — saves to disk after every `addMessage`.
3. **`CommitChatAgent`** — inline system prompt (no Jinja); sends `SystemMessage` + `conversation.messages` to the LLM.
4. **`ChatConfiguration`** — registers `AgentProcessChatbot` wired to the **Commit chat** agent only.
5. **`AgentProcessChatSession`** — adds user messages to the conversation and runs the agent; assistant replies are appended and persisted.

## Embabel guide mapping

- [Building Chatbots (§4.13)](https://docs.embabel.com/embabel-agent/guide/0.5.0-SNAPSHOT/) — `Conversation`, `Chatbot`, `conversationId` on `createSession`.
- Production Embabel apps often use **`embabel-chat-store`** (Neo4j). This demo uses **file JSON** with the same `ConversationFactory.load` / `create` contract.

## Spring AI comparison

| Spring AI | simple-demo |
|-----------|-------------|
| `InMemoryChatMemoryRepository` | JSON files under `~/.simple-demo/conversations` |
| `MessageWindowChatMemory` | Full history on disk; LLM prompt uses conversation messages |
| `conversationId` | Conversation id + `resume-chat` |

## Troubleshooting

| Problem | What to check |
|---------|----------------|
| `Agent Commit chat has no goals` | `CommitChatAgent` must use `PlannerType.UTILITY` (adds the synthetic Nirvana goal for open-ended chat) |
| Chat stuck after you type a message | Same as above — GOAP cannot plan chat turns; use UTILITY on both the `@Agent` and `Chatbot` bean |
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
