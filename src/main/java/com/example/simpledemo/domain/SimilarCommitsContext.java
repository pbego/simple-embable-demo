package com.example.simpledemo.domain;

/**
 * Vector-memory recall of similar past commit suggestions.
 */
public record SimilarCommitsContext(String content) {

  public boolean isEmpty() {
    return content == null || content.isBlank();
  }
}
