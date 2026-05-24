package com.example.simpledemo.git;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

class GitExecutorReadOnlyTest {

  @TempDir Path workTree;

  @Test
  void blocksMutatingSubcommandsWhenReadOnly() {
    var executor = new GitExecutor(workTree.toString(), true);
    var result = executor.runGit("push", "origin", "main");
    assertTrue(result.contains("blocked"));
  }
}
