package com.example.simpledemo.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Starts the app as an MCP server on a random port, then talks to it like Cursor or Claude would:
 * connect over SSE, list tools, call one. No LLM is involved in the tool call.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "simple-demo.mcp-server.enabled=true",
      "simple-demo.rag.enabled=false",
      "simple-demo.vector-memory.enabled=false",
      "embabel.agent.shell.interactive.enabled=false",
      "spring.shell.interactive.enabled=false",
      "spring.shell.noninteractive.enabled=false",
      "spring.main.web-application-type=servlet"
    })
@ActiveProfiles({"mcp-server", "file"})
class SimpleMcpToolsTest {

  @LocalServerPort int port;

  McpSyncClient client;

  @BeforeEach
  void connect() {
    var transport = HttpClientSseClientTransport.builder("http://localhost:" + port).build();
    client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).build();
    client.initialize();
  }

  @AfterEach
  void disconnect() {
    client.closeGracefully();
  }

  @Test
  void serverListsTheSimpleTools() {
    var names = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();

    assertThat(names).contains("add", "countWords");
  }

  @Test
  void addReturnsTheSum() {
    var result = client.callTool(new McpSchema.CallToolRequest("add", Map.of("a", 2, "b", 3)));

    assertThat(result.isError()).isNotEqualTo(Boolean.TRUE);
    assertThat(text(result)).isEqualTo("5");
  }

  @Test
  void countWordsCountsWords() {
    var result =
        client.callTool(
            new McpSchema.CallToolRequest("countWords", Map.of("text", "hello from the MCP demo")));

    assertThat(text(result)).isEqualTo("5");
  }

  private static String text(McpSchema.CallToolResult result) {
    return ((McpSchema.TextContent) result.content().get(0)).text();
  }
}
