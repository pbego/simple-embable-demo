package com.example.simpledemo.git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Runs read-only git commands in the configured work tree.
 */
@Component
public class GitExecutor {

  private final Path workTree;

  public GitExecutor(@Value("${simple-demo.git.work-tree:.}") String workTreePath) {
    this.workTree = Path.of(workTreePath).toAbsolutePath().normalize();
  }

  public Path workTree() {
    return workTree;
  }

  public String runGit(String... args) {
    var command = new String[args.length + 1];
    command[0] = "git";
    System.arraycopy(args, 0, command, 1, args.length);
    try {
      var process =
          new ProcessBuilder(command)
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
}
