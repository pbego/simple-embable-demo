package com.example.simpledemo.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.tool.Tool;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitRepositoryTest {

  @TempDir Path workTree;

  private GitRepository repository;

  @BeforeEach
  void setUpGitRepo() throws Exception {
    runInRepo("init");
    runInRepo("config", "user.email", "demo@example.com");
    runInRepo("config", "user.name", "Demo User");
    Files.writeString(workTree.resolve("README.md"), "hello\n");
    runInRepo("add", "README.md");
    runInRepo("commit", "-m", "Initial commit");

    repository = new GitRepository(new GitExecutor(workTree.toString()));
  }

  @Test
  void exposesFourLlmTools() {
    List<Tool> tools = Tool.fromInstance(repository);
    assertEquals(4, tools.size());
    var names = tools.stream().map(t -> t.getDefinition().getName()).toList();
    assertTrue(names.contains("currentBranch"));
    assertTrue(names.contains("shortStatus"));
    assertTrue(names.contains("lastCommit"));
    assertTrue(names.contains("listBranches"));
  }

  @Test
  void currentBranchToolReturnsMainOrMaster() {
    var result = invokeTool("currentBranch");
    assertTrue(result.equals("main") || result.equals("master"), "branch was: " + result);
  }

  @Test
  void lastCommitToolIncludesInitialCommitMessage() {
    var result = invokeTool("lastCommit");
    assertTrue(result.contains("Initial commit"), "last commit was: " + result);
  }

  private String invokeTool(String toolName) {
    var tool =
        Tool.fromInstance(repository).stream()
            .filter(t -> t.getDefinition().getName().equals(toolName))
            .findFirst()
            .orElseThrow();
    var callResult = tool.call("{}");
    return switch (callResult) {
      case Tool.Result.Text text -> text.getContent();
      case Tool.Result.WithArtifact artifact -> artifact.getContent();
      default -> throw new IllegalStateException("unexpected tool result: " + callResult);
    };
  }

  private void runInRepo(String... args) throws Exception {
    var command = new String[args.length + 1];
    command[0] = "git";
    System.arraycopy(args, 0, command, 1, args.length);
    var process =
        new ProcessBuilder(command).directory(workTree.toFile()).redirectErrorStream(true).start();
    var exit = process.waitFor();
    if (exit != 0) {
      var output = new String(process.getInputStream().readAllBytes());
      throw new IllegalStateException("git " + String.join(" ", args) + " failed: " + output);
    }
  }
}
