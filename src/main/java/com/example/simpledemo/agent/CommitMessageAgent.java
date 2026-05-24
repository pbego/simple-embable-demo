package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.git.GitChangesCollector;
import com.example.simpledemo.memory.CommitVectorMemory;
import com.example.simpledemo.rag.CommitStyleRetriever;
import com.example.simpledemo.template.JinjavaSafe;
import java.util.HashMap;

/**
 * Inspects git changes on the current branch (via {@link GitChangesCollector}) and
 * proposes a commit message using the configured local LLM.
 *
 * <p>The LLM prompt is rendered from {@code prompts/commit/generate_message.jinja}.
 */
@Agent(description = "Inspect git changes on the current branch and suggest a commit message")
public class CommitMessageAgent {

  private static final String COMMIT_MESSAGE_TEMPLATE = "commit/generate_message";
  private static final String NO_GIT_OUTPUT = "(no output)";

  public record Request(String question) {}

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

  /**
   * Single-shot entry for chat routing. The {@code x} command can still use the two-step planner
   * path ({@link #collectChanges} → {@link #generateCommitMessage}).
   */
  @Action(canRerun = true, description = "Suggest a commit message from current git changes")
  public CommitMessage answer(Request request, ActionContext context) {
    var userInput =
        new UserInput(request != null && request.question() != null ? request.question() : "");
    var changes = collectChanges(userInput);
    return generateCommitMessage(changes, userInput, context.ai());
  }

  @Action
  public GitChanges collectChanges(UserInput userInput) {
    return gitChangesCollector.collect(userInput.getContent());
  }

  @AchievesGoal(description = "Propose a commit message for the current changes")
  @Action
  @Export(remote = true)
  public CommitMessage generateCommitMessage(GitChanges changes, UserInput userInput, Ai ai) {
    var rawHint = changes.userHint().isBlank() ? userInput.getContent() : changes.userHint();
    var developerHint = rawHint == null ? "" : rawHint.trim();

    var styleGuide = commitStyleRetriever.retrieveStyleGuide(changes);
    var pastCommits = commitVectorMemory.recallSimilar(changes.status() + "\n" + changes.stagedDiff());

    var model = new HashMap<String, Object>();
    model.put("branch", changes.branch());
    model.put("status", JinjavaSafe.escape(changes.status()));
    model.put("changeSections", buildChangeSections(changes));
    model.put("developerSection", buildDeveloperSection(developerHint));
    model.put("styleGuideSection", buildStyleGuideSection(styleGuide));
    model.put("pastCommitsSection", buildPastCommitsSection(pastCommits));

    var commit =
        ai.withDefaultLlm()
            .rendering(COMMIT_MESSAGE_TEMPLATE)
            .createObject(CommitMessage.class, model);

    commitVectorMemory.remember(commit, changes.branch());
    return commit;
  }

  private static String buildChangeSections(GitChanges changes) {
    var sections = new StringBuilder();
    if (hasDiff(changes.stagedDiff())) {
      sections.append("## Staged diff (git diff --staged)\n");
      sections.append(JinjavaSafe.escape(changes.stagedDiff()));
    } else {
      sections.append("## Staged diff\n(no staged changes)");
    }
    sections.append("\n\n");
    if (hasDiff(changes.unstagedDiff())) {
      sections.append("## Unstaged diff (git diff)\n");
      sections.append(JinjavaSafe.escape(changes.unstagedDiff()));
    } else {
      sections.append("## Unstaged diff\n(no unstaged changes)");
    }
    return sections.toString();
  }

  private static String buildDeveloperSection(String developerHint) {
    if (developerHint.isBlank()) {
      return "";
    }
    return """
        ## Developer instructions (MUST follow — override default wording inferred from diffs)
        %s
        """
        .formatted(JinjavaSafe.escape(developerHint));
  }

  private static String buildStyleGuideSection(String styleGuide) {
    if (styleGuide == null || styleGuide.isBlank()) {
      return "";
    }
    return "## Repository style guide (from RAG index)\n" + JinjavaSafe.escape(styleGuide) + "\n";
  }

  private static String buildPastCommitsSection(String pastCommits) {
    if (pastCommits == null || pastCommits.isBlank()) {
      return "";
    }
    return "## Similar past commits (vector memory)\n" + JinjavaSafe.escape(pastCommits) + "\n";
  }

  private static boolean hasDiff(String diff) {
    return diff != null && !diff.isBlank() && !NO_GIT_OUTPUT.equals(diff);
  }
}
