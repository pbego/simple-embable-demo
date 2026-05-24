package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.example.simpledemo.domain.RepositorySnapshot;

/**
 * Reviews diffs for obvious security issues before proposing a commit message.
 */
@Agent(description = "Review git changes for security concerns before committing")
public class SecurityReviewAgent {

  @AchievesGoal(description = "Review git changes for security concerns")
  @Action
  public String reviewChanges(RepositorySnapshot snapshot, Ai ai) {
    var combined = snapshot.stagedDiff() + "\n" + snapshot.unstagedDiff();
    var diffSample =
        combined.length() <= 8000 ? combined : combined.substring(0, 8000) + "\n... (truncated)";

    return ai.withDefaultLlm()
        .createObject(
            """
            You are a security reviewer. Scan the git diff for secrets, credentials, private keys,
            API tokens, or unsafe patterns. Be concise.

            Branch: %s
            Status:
            %s

            Diff sample:
            %s

            Reply with either "OK: no concerns" or "WARN:" followed by bullet points.
            """
                .formatted(snapshot.branch(), snapshot.status(), diffSample),
            String.class);
  }
}
