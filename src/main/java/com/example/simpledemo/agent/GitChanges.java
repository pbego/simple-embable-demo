package com.example.simpledemo.agent;

/**
 * Snapshot of the current branch and working-tree changes from git.
 */
public record GitChanges(
    String branch,
    String status,
    String stagedDiff,
    String unstagedDiff,
    String userHint) {
}
