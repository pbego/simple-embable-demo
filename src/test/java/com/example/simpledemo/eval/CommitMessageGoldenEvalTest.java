package com.example.simpledemo.eval;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.agent.CommitMessage;
import com.example.simpledemo.agent.CommitMessageAgent;
import com.example.simpledemo.agent.GitChanges;
import com.example.simpledemo.memory.CommitVectorMemory;
import com.example.simpledemo.rag.CommitStyleRetriever;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CommitMessageGoldenEvalTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  record GoldenCase(String status, String stagedDiff, String expectedSubjectPattern) {}

  static List<GoldenCase> goldenCases() throws Exception {
    try (var stream =
        CommitMessageGoldenEvalTest.class.getResourceAsStream("/eval/commit-golden.json")) {
      return MAPPER.readValue(stream, new TypeReference<>() {});
    }
  }

  @ParameterizedTest
  @MethodSource("goldenCases")
  void subjectMatchesGoldenPattern(GoldenCase golden) {
    var gitCollector = mock(com.example.simpledemo.git.GitChangesCollector.class);
    when(gitCollector.collect(any()))
        .thenReturn(
            new GitChanges("main", golden.status(), golden.stagedDiff(), "", ""));

    var styleRetriever = mock(CommitStyleRetriever.class);
    when(styleRetriever.retrieveStyleGuide(any())).thenReturn("");

    var vectorMemory = mock(CommitVectorMemory.class);
    when(vectorMemory.recallSimilar(any())).thenReturn("");

    var agent = new CommitMessageAgent(gitCollector, styleRetriever, vectorMemory);

    var ai = mock(Ai.class);
    var promptRunner = mock(PromptRunner.class);
    var rendering = mock(PromptRunner.Rendering.class);
    when(ai.withDefaultLlm()).thenReturn(promptRunner);
    when(promptRunner.rendering(any())).thenReturn(rendering);

    var subject = golden.expectedSubjectPattern().contains("docs") ? "docs: add guide section" : "fix: handle error";
    when(rendering.createObject(eq(CommitMessage.class), any()))
        .thenReturn(new CommitMessage(subject, "body"));

    var changes = new GitChanges("main", golden.status(), golden.stagedDiff(), "", "");
    var result = agent.generateCommitMessage(changes, new UserInput(""), ai);

    assertTrue(
        result.subject().matches(golden.expectedSubjectPattern()),
        () -> "subject '" + result.subject() + "' did not match " + golden.expectedSubjectPattern());
  }
}
