package com.example.simpledemo.invocation;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.agent.CommitMessage;
import com.example.simpledemo.domain.CommitProposal;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Programmatic entry point for CI hooks and shell commands.
 */
@Component
public class CommitInvocationRunner {

  private final AgentPlatform agentPlatform;

  public CommitInvocationRunner(AgentPlatform agentPlatform) {
    this.agentPlatform = agentPlatform;
  }

  public CommitProposal run(String hint) throws Exception {
    var process =
        AgentInvocation.create(agentPlatform, CommitMessage.class)
            .runAsync(new UserInput(hint == null ? "" : hint))
            .get(5, TimeUnit.MINUTES);

    while (!process.getFinished()) {
      Thread.sleep(200);
    }

    var message = process.resultOfType(CommitMessage.class);
    return CommitProposal.from(message);
  }
}
