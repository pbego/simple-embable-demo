package com.example.simpledemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "embabel.agent.shell.interactive.enabled=false",
    "spring.shell.interactive.enabled=false",
    "spring.shell.noninteractive.enabled=false"
})
class SimpleDemoApplicationTests {

  @Test
  void contextLoads() {
  }

}
