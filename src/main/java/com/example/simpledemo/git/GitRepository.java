package com.example.simpledemo.git;

import com.embabel.agent.api.annotation.LlmTool;

/**
 * Domain object exposing read-only git facts to the LLM via {@link LlmTool}.
 *
 * <p>Pass an instance to {@code ai.withToolObject(...)} so the model can call real git instead of
 * guessing branch names or commit hashes.
 */
public class GitRepository {

  private final GitExecutor git;

  public GitRepository(GitExecutor git) {
    this.git = git;
  }

  @LlmTool(description = "Returns the name of the current git branch")
  public String currentBranch() {
    return git.runGit("branch", "--show-current");
  }

  @LlmTool(description = "Returns git status in short format (same as git status --short)")
  public String shortStatus() {
    return git.runGit("status", "--short");
  }

  @LlmTool(description = "Returns the latest commit as a one-line summary (hash and subject)")
  public String lastCommit() {
    return git.runGit("log", "-1", "--oneline");
  }

  @LlmTool(description = "Lists local branch names")
  public String listBranches() {
    return git.runGit("branch", "--list");
  }
}
