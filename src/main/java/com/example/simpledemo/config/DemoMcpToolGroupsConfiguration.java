package com.example.simpledemo.config;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import io.modelcontextprotocol.client.McpSyncClient;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mcp")
@ConditionalOnBean(McpSyncClient.class)
public class DemoMcpToolGroupsConfiguration {

  @Bean
  ToolGroup filesystemMcpToolGroup(List<McpSyncClient> mcpSyncClients) {
    return new McpToolGroup(
        ToolGroupDescription.create(
            "filesystem", "Read files from allowed directories via MCP filesystem server"),
        "filesystem-mcp",
        "MCP",
        Set.of(ToolGroupPermission.HOST_ACCESS),
        mcpSyncClients,
        callback -> callback.getToolDefinition().name().contains("read"),
        null);
  }
}
