package com.example.simpledemo.domain;

/**
 * RAG-derived repository commit conventions.
 */
public record StyleGuideContext(String content) {

  public boolean isEmpty() {
    return content == null || content.isBlank();
  }
}
