package com.example.simpledemo.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CommitMessageTest {

  @Test
  void formattedSubjectOnly() {
    assertEquals("feat: add router", new CommitMessage("feat: add router", "").formatted());
  }

  @Test
  void formattedSubjectAndBody() {
    var text = new CommitMessage("feat: add router", "Wire commit flow in chat.").formatted();
    assertTrue(text.startsWith("Subject: feat: add router"));
    assertTrue(text.contains("Wire commit flow in chat."));
  }
}
