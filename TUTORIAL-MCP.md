# MCP — consume & publish

External tools in (filesystem MCP) and agents out (SSE MCP server). Includes RAG + vector memory from earlier branches.

## Overview

Until now most logic runs **inside** the JVM: git, Lucene, Ollama. Production assistants also need **external systems** and **IDE integration** (Cursor, Claude Desktop).

**Model Context Protocol (MCP)** standardizes both directions:

| Direction | What you learn |
|-----------|----------------|
| **Consume** | Embabel **tool groups** backed by MCP clients (filesystem) |
| **Publish** | `@Export(remote = true)` goals as MCP tools over SSE |

Default `./mvnw spring-boot:run` keeps the **interactive shell**. MCP uses Spring profiles so CI does not need `npx` or port 8081.

**Embabel guide:** MCP consuming, MCP server starter, tool groups.

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| RAG | Run `rag-index` for richer commits |
| `npx` | Consume profile — filesystem MCP |
| Port `8081` | `mcp-server` profile |

## Key code

| File | Role |
|------|------|
| `application-mcp.properties` | MCP client (stdio filesystem) |
| `application-mcp-server.properties` | MCP server on 8081 |
| `config/DemoMcpToolGroupsConfiguration.java` | `McpToolGroup` |
| `agent/McpFilesystemAgent.java` | `withToolGroup("filesystem")` |
| `CommitMessageAgent` / `CommitStyleAgent` | `@Export(remote = true)` |

## Consume MCP tools

```bash
SPRING_PROFILES_ACTIVE=mcp ./mvnw spring-boot:run
```

```text
shell:> x "read docs/COMMIT_CONVENTIONS.md and summarize commit rules"
```

Optional GitHub MCP: see comments in `application-mcp.properties`.

## Publish as MCP server

```bash
SPRING_PROFILES_ACTIVE=mcp-server ./mvnw spring-boot:run
```

| Setting | Value |
|---------|--------|
| SSE URL | `http://localhost:8081/sse` |
| Server type | `spring.ai.mcp.server.type=SYNC` |

**Cursor** (`~/.cursor/mcp.json`):

```json
{
  "mcpServers": {
    "simple-demo-commit": {
      "url": "http://localhost:8081/sse"
    }
  }
}
```

## Profiles

| Profile | Behavior |
|---------|----------|
| *(default)* | Shell, RAG, vector memory |
| `mcp` | + filesystem MCP client |
| `mcp-server` | HTTP MCP server (shell off) |

Branch index on `main`: [README.md](https://github.com/pbego/simple-embable-demo/blob/main/README.md#feature-branches).
