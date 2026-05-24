package com.example.simpledemo.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.domain.CommitRequest;
import com.example.simpledemo.domain.RepositorySnapshot;
import com.example.simpledemo.domain.SimilarCommitsContext;
import com.example.simpledemo.domain.StyleGuideContext;
import com.example.simpledemo.memory.CommitVectorMemory;
import com.example.simpledemo.rag.CommitStyleRetriever;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommitMessageAgentTest {

  private CommitMessageAgent agent;
  private Ai ai;
  private PromptRunner promptRunner;
  private PromptRunner.Rendering rendering;

  @BeforeEach
  void setUp() {
    var gitCollector = mock(com.example.simpledemo.git.GitChangesCollector.class);
    when(gitCollector.collect(any()))
        .thenReturn(
            new GitChanges(
                "main",
                " M README.md",
                "diff --git a/README.md\n+feature",
                "",
                ""));

    var styleRetriever = mock(CommitStyleRetriever.class);
    when(styleRetriever.retrieveStyleGuide(any())).thenReturn("");

    var vectorMemory = mock(CommitVectorMemory.class);
    when(vectorMemory.recallSimilar(any())).thenReturn("");

    agent = new CommitMessageAgent(gitCollector, styleRetriever, vectorMemory);

    ai = mock(Ai.class);
    promptRunner = mock(PromptRunner.class);
    rendering = mock(PromptRunner.Rendering.class);
    when(ai.withDefaultLlm()).thenReturn(promptRunner);
    when(promptRunner.rendering(any())).thenReturn(rendering);
    when(rendering.createObject(eq(CommitMessage.class), any()))
        .thenReturn(new CommitMessage("fix: update readme", "Document router changes."));
  }

  @Test
  void proposeCommitUsesMockLlm() {
    var snapshot =
        new RepositorySnapshot("main", " M README.md", "+lines", "", "");
    var result =
        agent.proposeCommit(
            snapshot,
            new CommitRequest("focus on docs"),
            new StyleGuideContext(""),
            new SimilarCommitsContext(""),
            ai);

    assertNotNull(result);
    assertFalse(result.subject().isBlank());
  }

  @Test
  void generateCommitMessageFromGitChanges() {
    var changes = new GitChanges("main", " M README.md", "+lines", "", "");
    var result = agent.generateCommitMessage(changes, new UserInput("focus on docs"), ai);
    assertNotNull(result);
    assertFalse(result.subject().isBlank());
  }
}
