package com.example.simpledemo.domain;

import com.example.simpledemo.agent.GitChanges;

/**
 * Typed view of the working tree at commit time.
 */
public record RepositorySnapshot(
    String branch, String status, String stagedDiff, String unstagedDiff, String userHint) {

  public static RepositorySnapshot from(GitChanges changes) {
    return new RepositorySnapshot(
        changes.branch(),
        changes.status(),
        changes.stagedDiff(),
        changes.unstagedDiff(),
        changes.userHint());
  }

  public GitChanges toGitChanges() {
    return new GitChanges(branch, status, stagedDiff, unstagedDiff, userHint);
  }
}
