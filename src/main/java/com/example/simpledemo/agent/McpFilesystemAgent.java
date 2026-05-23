package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.context.annotation.Profile;

/**
 * Demonstrates consuming MCP tools (tutorial 21) when {@code mcp} profile is active.
 */
@Agent(description = "Read files from the filesystem using MCP tools")
@Profile("mcp")
public class McpFilesystemAgent {

  @AchievesGoal(description = "Read or summarize a file path using MCP filesystem tools")
  @Action
  public String readViaMcp(UserInput userInput, Ai ai) {
    var question = userInput.getContent() == null ? "" : userInput.getContent().trim();
    return ai.withDefaultLlm()
        .withToolGroup("filesystem")
        .createObject(
            """
            You help developers inspect files. Use MCP filesystem tools to read paths.
            Do not invent file contents. Prefer paths under the user's home directory.

            Request:
            %s
            """
                .formatted(question.isEmpty() ? "List what you can access and suggest docs/COMMIT_CONVENTIONS.md" : question),
            String.class);
  }
}
