package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.domain.CommitProposal;
import com.example.simpledemo.domain.CommitRequest;

/**
 * Multi-agent pipeline: security review → changelog → commit message (in-process A2A shape).
 */
@Agent(description = "Run security review and changelog before proposing a commit message")
public class CommitOrchestratorAgent {

  private final CommitMessageAgent commitMessageAgent;
  private final SecurityReviewAgent securityReviewAgent;
  private final ChangelogAgent changelogAgent;

  public CommitOrchestratorAgent(
      CommitMessageAgent commitMessageAgent,
      SecurityReviewAgent securityReviewAgent,
      ChangelogAgent changelogAgent) {
    this.commitMessageAgent = commitMessageAgent;
    this.securityReviewAgent = securityReviewAgent;
    this.changelogAgent = changelogAgent;
  }

  @Action
  public OrchestratorContext collect(UserInput userInput) {
    var snapshot = commitMessageAgent.captureRepository(userInput);
    return new OrchestratorContext(snapshot, userInput.getContent());
  }

  @Action
  public OrchestratorContext withSecurityReview(OrchestratorContext context, Ai ai) {
    var review = securityReviewAgent.reviewChanges(context.snapshot(), ai);
    return context.withSecurityReview(review);
  }

  @Action
  public OrchestratorContext withChangelog(OrchestratorContext context, Ai ai) {
    var changelog = changelogAgent.summarizeForChangelog(context.snapshot(), ai);
    return context.withChangelog(changelog);
  }

  @AchievesGoal(description = "Orchestrated commit message with security and changelog context")
  @Action
  public CommitProposal proposeWithContext(OrchestratorContext context, Ai ai) {
    var hint =
        """
        %s

        Security review:
        %s

        Changelog summary:
        %s
        """
            .formatted(
                context.userHint(),
                context.securityReview(),
                context.changelog());

    var request = new CommitRequest(hint);
    var style = commitMessageAgent.loadStyleGuide(context.snapshot());
    var similar = commitMessageAgent.loadSimilarCommits(context.snapshot());
    return commitMessageAgent.proposeCommit(context.snapshot(), request, style, similar, ai);
  }

  public record OrchestratorContext(
      com.example.simpledemo.domain.RepositorySnapshot snapshot,
      String userHint,
      String securityReview,
      String changelog) {

    public OrchestratorContext(
        com.example.simpledemo.domain.RepositorySnapshot snapshot, String userHint) {
      this(snapshot, userHint == null ? "" : userHint, "", "");
    }

    public OrchestratorContext withSecurityReview(String review) {
      return new OrchestratorContext(snapshot, userHint, review, changelog);
    }

    public OrchestratorContext withChangelog(String entry) {
      return new OrchestratorContext(snapshot, userHint, securityReview, entry);
    }
  }
}
