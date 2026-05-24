package com.example.simpledemo.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.example.simpledemo.git.GitExecutor;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitInfoAgentTest {

  @TempDir Path workTree;

  @Test
  void answerAboutRepoPassesGitToolsToLlm() {
    var agent = new GitInfoAgent(new GitExecutor(workTree.toString(), false));
    var context = FakeOperationContext.create();
    context.expectResponse("No commits yet.");

    var answer = agent.answerAboutRepo(new UserInput("what branch am I on?"), context.ai());

    assertEquals("No commits yet.", answer);
    assertEquals(1, context.getLlmInvocations().size());
    assertFalse(context.getLlmInvocations().getFirst().getInteraction().getTools().isEmpty());
  }
}
