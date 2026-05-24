package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.example.simpledemo.domain.RepositorySnapshot;

/**
 * Summarizes user-visible changes for changelog or PR descriptions.
 */
@Agent(description = "Summarize git changes for a changelog snippet")
public class ChangelogAgent {

  @AchievesGoal(description = "Summarize changes for a changelog entry")
  @Action
  public String summarizeForChangelog(RepositorySnapshot snapshot, Ai ai) {
    return ai.withDefaultLlm()
        .createObject(
            """
            Write a short changelog entry (2-4 bullets) for the changes below.
            Focus on user-visible behavior, not internal refactors.

            Branch: %s
            Status:
            %s

            Staged diff:
            %s

            Unstaged diff:
            %s
            """
                .formatted(
                    snapshot.branch(),
                    snapshot.status(),
                    snapshot.stagedDiff(),
                    snapshot.unstagedDiff()),
            String.class);
  }
}
