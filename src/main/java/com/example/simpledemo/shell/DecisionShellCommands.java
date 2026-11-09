package com.example.simpledemo.shell;

import com.example.simpledemo.decision.CommitTypeClassifier;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
public class DecisionShellCommands {

  private final CommitTypeClassifier classifier;

  public DecisionShellCommands(CommitTypeClassifier classifier) {
    this.classifier = classifier;
  }

  @Command(
      name = "commit-type",
      description = "Pick the Conventional Commits type of a change with the decision model (no LLM)")
  public String commitType(
      @Argument(index = 0, description = "The change to classify, in plain words") String change) {
    return classifier.classify(change).formatted();
  }
}
