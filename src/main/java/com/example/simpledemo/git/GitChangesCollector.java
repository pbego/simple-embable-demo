package com.example.simpledemo.git;

import com.example.simpledemo.agent.GitChanges;
import org.springframework.stereotype.Component;

/**
 * Collects working-tree changes from the local git repository.
 */
@Component
public class GitChangesCollector {

  private static final int MAX_DIFF_CHARS = 12_000;

  private final GitExecutor gitExecutor;

  public GitChangesCollector(GitExecutor gitExecutor) {
    this.gitExecutor = gitExecutor;
  }

  public GitChanges collect(String userHint) {
    var branch = gitExecutor.runGit("branch", "--show-current");
    var status = gitExecutor.runGit("status", "--short");
    var stagedDiff = truncate(gitExecutor.runGit("diff", "--staged"));
    var unstagedDiff = truncate(gitExecutor.runGit("diff"));
    return new GitChanges(branch, status, stagedDiff, unstagedDiff, userHint == null ? "" : userHint.trim());
  }

  private static String truncate(String text) {
    if (text.length() <= MAX_DIFF_CHARS) {
      return text;
    }
    return text.substring(0, MAX_DIFF_CHARS)
        + "\n\n... (diff truncated at %d characters)".formatted(MAX_DIFF_CHARS);
  }
}
