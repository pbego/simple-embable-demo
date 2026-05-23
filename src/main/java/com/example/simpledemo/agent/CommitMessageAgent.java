package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.git.GitChangesCollector;
import com.example.simpledemo.memory.CommitVectorMemory;
import com.example.simpledemo.rag.CommitStyleRetriever;

/**
 * Inspects git changes on the current branch (via {@link GitChangesCollector}) and
 * proposes a commit message using the configured local LLM.
 */
@Agent(description = "Inspect git changes on the current branch and suggest a commit message")
public class CommitMessageAgent {

  private final GitChangesCollector gitChangesCollector;
  private final CommitStyleRetriever commitStyleRetriever;
  private final CommitVectorMemory commitVectorMemory;

  public CommitMessageAgent(
      GitChangesCollector gitChangesCollector,
      CommitStyleRetriever commitStyleRetriever,
      CommitVectorMemory commitVectorMemory) {
    this.gitChangesCollector = gitChangesCollector;
    this.commitStyleRetriever = commitStyleRetriever;
    this.commitVectorMemory = commitVectorMemory;
  }

  @Action
  public GitChanges collectChanges(UserInput userInput) {
    return gitChangesCollector.collect(userInput.getContent());
  }

  @AchievesGoal(description = "Propose a commit message for the current changes")
  @Action
  @Export(remote = true)
  public CommitMessage generateCommitMessage(GitChanges changes, UserInput userInput, Ai ai) {
    var hint = changes.userHint().isBlank() ? userInput.getContent() : changes.userHint();
    var hintBlock = hint.isBlank()
        ? ""
        : "\nAdditional instructions from the developer:\n%s\n".formatted(hint);

    var styleGuide = commitStyleRetriever.retrieveStyleGuide(changes);
    var styleBlock =
        styleGuide.isBlank()
            ? ""
            : """
            ## Repository style guide (from RAG index)
            %s

            """
                .formatted(styleGuide);

    var pastCommits = commitVectorMemory.recallSimilar(changes.status() + "\n" + changes.stagedDiff());
    var memoryBlock =
        pastCommits.isBlank()
            ? ""
            : """
            ## Similar past commits (vector memory)
            %s

            """
                .formatted(pastCommits);

    var commit =
        ai.withDefaultLlm()
        .createObject("""
            You are helping a developer write a git commit message.

            Follow the repository style guide below when present; otherwise use Conventional Commits
            (e.g. feat:, fix:, chore:, docs:). Subject: imperative mood, max 72 characters, no period.
            Body: explain what and why, wrapped at ~72 characters per line if needed.

            %s
            %s
            Current branch: %s

            git status --short:
            %s

            Staged diff (git diff --staged):
            %s

            Unstaged diff (git diff):
            %s
            %s
            If there are no meaningful changes, set subject to "chore: no changes to commit"
            and body to a short explanation.

            Return JSON with fields: subject (string), body (string, may be empty).
            """.formatted(
            styleBlock,
            memoryBlock,
            changes.branch(),
            changes.status(),
            changes.stagedDiff(),
            changes.unstagedDiff(),
            hintBlock),
        CommitMessage.class);

    commitVectorMemory.remember(commit, changes.branch());
    return commit;
  }
}
