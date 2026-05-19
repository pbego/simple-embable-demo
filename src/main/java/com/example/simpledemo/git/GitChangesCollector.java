package com.example.simpledemo.git;

import com.example.simpledemo.agent.GitChanges;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Collects working-tree changes from the local git repository.
 */
@Component
public class GitChangesCollector {

  private static final int MAX_DIFF_CHARS = 12_000;

  private final Path workTree;

  public GitChangesCollector(
      @Value("${simple-demo.git.work-tree:.}") String workTreePath) {
    this.workTree = Path.of(workTreePath).toAbsolutePath().normalize();
  }

  public GitChanges collect(String userHint) {
    var branch = runGit("branch", "--show-current");
    var status = runGit("status", "--short");
    var stagedDiff = truncate(runGit("diff", "--staged"));
    var unstagedDiff = truncate(runGit("diff"));
    return new GitChanges(branch, status, stagedDiff, unstagedDiff, userHint == null ? "" : userHint.trim());
  }

  private String runGit(String... args) {
    var command = new String[args.length + 1];
    command[0] = "git";
    System.arraycopy(args, 0, command, 1, args.length);
    try {
      var process = new ProcessBuilder(command)
          .directory(workTree.toFile())
          .redirectErrorStream(true)
          .start();
      var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
      var exitCode = process.waitFor();
      if (exitCode != 0 && output.isEmpty()) {
        return "(git command failed: %s, exit %d)".formatted(String.join(" ", command), exitCode);
      }
      return output.isEmpty() ? "(no output)" : output;
    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      return "(failed to run git: %s)".formatted(e.getMessage());
    }
  }

  private static String truncate(String text) {
    if (text.length() <= MAX_DIFF_CHARS) {
      return text;
    }
    return text.substring(0, MAX_DIFF_CHARS)
        + "\n\n... (diff truncated at %d characters)".formatted(MAX_DIFF_CHARS);
  }
}
