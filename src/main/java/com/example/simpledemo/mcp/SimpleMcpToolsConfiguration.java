package com.example.simpledemo.mcp;

import com.embabel.agent.api.tool.ToolObject;
import com.embabel.agent.mcpserver.McpToolExport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes {@link SimpleMcpTools} on the MCP server.
 *
 * <p>Embabel's MCP server collects every {@link McpToolExport} bean and advertises its tools. Only
 * active when the server is on ({@code mcp-server} profile).
 */
@Configuration
@ConditionalOnProperty(name = "simple-demo.mcp-server.enabled", havingValue = "true")
public class SimpleMcpToolsConfiguration {

  @Bean
  McpToolExport simpleMcpTools() {
    return McpToolExport.fromToolObject(new ToolObject(new SimpleMcpTools()));
  }
}
