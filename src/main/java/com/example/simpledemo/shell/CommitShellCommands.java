package com.example.simpledemo.shell;

import com.example.simpledemo.invocation.CommitInvocationRunner;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class CommitShellCommands {

  private final CommitInvocationRunner commitInvocationRunner;

  public CommitShellCommands(CommitInvocationRunner commitInvocationRunner) {
    this.commitInvocationRunner = commitInvocationRunner;
  }

  @ShellMethod(key = "commit-now", value = "Generate a commit message via AgentInvocation (no chat)")
  public String commitNow(
      @ShellOption(defaultValue = "", help = "Optional hint for the commit message") String hint)
      throws Exception {
    var proposal = commitInvocationRunner.run(hint);
    return proposal.formatted();
  }
}
