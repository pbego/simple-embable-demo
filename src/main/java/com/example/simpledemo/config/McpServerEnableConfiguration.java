package com.example.simpledemo.config;

import com.embabel.agent.autoconfigure.mcpserver.AgentMcpServerAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "simple-demo.mcp-server.enabled", havingValue = "true")
@Import(AgentMcpServerAutoConfiguration.class)
public class McpServerEnableConfiguration {}
