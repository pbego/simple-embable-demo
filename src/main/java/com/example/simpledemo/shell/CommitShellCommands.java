package com.example.simpledemo.shell;

import com.example.simpledemo.invocation.CommitInvocationRunner;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
public class CommitShellCommands {

  private final CommitInvocationRunner commitInvocationRunner;

  public CommitShellCommands(CommitInvocationRunner commitInvocationRunner) {
    this.commitInvocationRunner = commitInvocationRunner;
  }

  @Command(name = "commit-now", description = "Generate a commit message via AgentInvocation (no chat)")
  public String commitNow(
      @Argument(index = 0, defaultValue = "", description = "Optional hint for the commit message") String hint)
      throws Exception {
    var proposal = commitInvocationRunner.run(hint);
    return proposal.formatted();
  }
}
