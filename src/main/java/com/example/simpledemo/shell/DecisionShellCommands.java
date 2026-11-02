package com.example.simpledemo.shell;

import com.example.simpledemo.decision.CommitTypeClassifier;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class DecisionShellCommands {

  private final CommitTypeClassifier classifier;

  public DecisionShellCommands(CommitTypeClassifier classifier) {
    this.classifier = classifier;
  }

  @ShellMethod(
      key = "commit-type",
      value = "Pick the Conventional Commits type of a change with the decision model (no LLM)")
  public String commitType(String change) {
    return classifier.classify(change).formatted();
  }
}
