package com.example.simpledemo.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatRouterTest {

  @Test
  void parseMessageExplicitCommitPrefix() {
    var parsed = ChatRouter.parseMessage("@commit focus on API changes");
    assertTrue(parsed.explicitTarget().isPresent());
    assertEquals(ChatRouter.RouteTarget.COMMIT, parsed.explicitTarget().get());
    assertEquals("focus on API changes", parsed.question());
  }

  @Test
  void parseMessageExplicitStylePrefix() {
    var parsed = ChatRouter.parseMessage("@style how do we format commits?");
    assertEquals(ChatRouter.RouteTarget.STYLE, parsed.explicitTarget().orElseThrow());
    assertEquals("how do we format commits?", parsed.question());
  }

  @Test
  void parseMessageExplicitJokePrefix() {
    var parsed = ChatRouter.parseMessage("@joke about kubernetes");
    assertEquals(ChatRouter.RouteTarget.JOKE, parsed.explicitTarget().orElseThrow());
    assertEquals("about kubernetes", parsed.question());
  }

  @Test
  void parseMessageExplicitGreetOnly() {
    var parsed = ChatRouter.parseMessage("@greet");
    assertEquals(ChatRouter.RouteTarget.GREETING, parsed.explicitTarget().orElseThrow());
    assertEquals("", parsed.question());
  }

  @Test
  void parseMessageNoPrefixUsesLlmRouting() {
    var parsed = ChatRouter.parseMessage("write a commit message for my staged files");
    assertTrue(parsed.explicitTarget().isEmpty());
    assertEquals("write a commit message for my staged files", parsed.question());
  }

  @Test
  void inferTargetsFromMessageRoutesStyleQuestions() {
    var targets = ChatRouter.inferTargetsFromMessage("how do we format commits?");
    assertEquals(java.util.List.of(ChatRouter.RouteTarget.STYLE), targets);
  }

  @Test
  void inferTargetsFromMessageDetectsCommitAndJoke() {
    var targets =
        ChatRouter.inferTargetsFromMessage(
            "give me a better commit message and also tell me a joke about java");
    assertEquals(
        java.util.List.of(ChatRouter.RouteTarget.COMMIT, ChatRouter.RouteTarget.JOKE), targets);
  }

  @Test
  void toDecisionUsesTargetsList() {
    var decision =
        ChatRouter.toDecision(
            new ChatRouter.LlmRoutingDecision(null, java.util.List.of("commit", "joke"), "both"),
            "ignored");
    assertEquals(
        java.util.List.of(ChatRouter.RouteTarget.COMMIT, ChatRouter.RouteTarget.JOKE),
        decision.targets());
  }

  @Test
  void toDecisionFallsBackToSingleTargetField() {
    var decision =
        ChatRouter.toDecision(new ChatRouter.LlmRoutingDecision("commit", null, "git"), "ignored");
    assertEquals(java.util.List.of(ChatRouter.RouteTarget.COMMIT), decision.targets());
  }

  @Test
  void resolveTargetsAugmentsWhenMessageMentionsCommitAndJoke() {
    var targets =
        ChatRouter.resolveTargets(
            new ChatRouter.LlmRoutingDecision("commit", null, "picked commit only"),
            "give me a better commit message and also tell me a joke about java");
    assertEquals(
        java.util.List.of(ChatRouter.RouteTarget.COMMIT, ChatRouter.RouteTarget.JOKE), targets);
  }

  @Test
  void targetFromTagAliases() {
    assertEquals(ChatRouter.RouteTarget.COMMIT, ChatRouter.targetFromTag("git").orElseThrow());
    assertEquals(ChatRouter.RouteTarget.STYLE, ChatRouter.targetFromTag("rag").orElseThrow());
    assertEquals(ChatRouter.RouteTarget.JOKE, ChatRouter.targetFromTag("funny").orElseThrow());
    assertTrue(ChatRouter.targetFromTag("unknown").isEmpty());
  }
}
