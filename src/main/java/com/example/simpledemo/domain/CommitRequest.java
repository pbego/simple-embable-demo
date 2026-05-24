package com.example.simpledemo.domain;

/**
 * Typed developer intent for commit generation (DICE-style context).
 */
public record CommitRequest(String hint) {

  public CommitRequest {
    hint = hint == null ? "" : hint.trim();
  }
}
