package com.example.simpledemo.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChatRouterTest {

  @Test
  void fromMessageRoutesJokes() {
    assertEquals(ChatRouter.RouteTarget.JOKE, ChatRouter.fromMessage("tell me a joke"));
    assertEquals(ChatRouter.RouteTarget.JOKE, ChatRouter.fromMessage("something funny please"));
  }

  @Test
  void fromMessageRoutesGitToCommitAgent() {
    assertEquals(ChatRouter.RouteTarget.COMMIT, ChatRouter.fromMessage("commit message"));
    assertEquals(ChatRouter.RouteTarget.COMMIT, ChatRouter.fromMessage("what does git status mean"));
  }

  @Test
  void fromMessageDefaultsToGreeting() {
    assertEquals(ChatRouter.RouteTarget.GREETING, ChatRouter.fromMessage("hello there"));
    assertEquals(ChatRouter.RouteTarget.GREETING, ChatRouter.fromMessage(""));
  }
}
