package com.example.simpledemo.mcp;

import com.embabel.agent.api.annotation.LlmTool;
import java.util.Arrays;

/**
 * The smallest MCP example in this project: two plain Java methods that any MCP client can call.
 *
 * <p>No LLM, no agent, no git. {@link SimpleMcpToolsConfiguration} publishes these methods on the
 * {@code mcp-server} profile, next to the exported agent goals. See TUTORIAL-MCP-SIMPLE.md.
 */
public class SimpleMcpTools {

  @LlmTool(description = "Adds two whole numbers and returns the sum")
  public int add(
      @LlmTool.Param(description = "First number") int a,
      @LlmTool.Param(description = "Second number") int b) {
    return a + b;
  }

  @LlmTool(description = "Counts the words in a piece of text")
  public int countWords(@LlmTool.Param(description = "The text to count") String text) {
    if (text == null || text.isBlank()) {
      return 0;
    }
    return (int) Arrays.stream(text.trim().split("\\s+")).count();
  }
}
