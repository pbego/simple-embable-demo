package com.example.simpledemo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    properties = {
      "simple-demo.mcp-server.enabled=true",
      "simple-demo.rag.enabled=false",
      "simple-demo.vector-memory.enabled=false",
      "embabel.agent.shell.interactive.enabled=false",
      "spring.shell.interactive.enabled=false",
      "spring.shell.noninteractive.enabled=false",
      "spring.main.web-application-type=servlet",
      "server.port=0"
    })
@ActiveProfiles({"mcp-server", "file"})
class McpServerProfileTest {

  @Test
  void contextLoadsWithMcpServerProfile() {
    assertTrue(true);
  }
}
