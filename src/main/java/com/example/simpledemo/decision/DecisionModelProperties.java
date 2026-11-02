package com.example.simpledemo.decision;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where the decision model listens, and how sure it must be before its answer is used.
 *
 * @param url base URL of a server speaking {@code POST /v1/systemone} (Von, Laya)
 * @param minConfidence the chosen option's probability must reach this, or the answer is "unsure"
 */
@ConfigurationProperties(prefix = "simple-demo.decision-model")
public record DecisionModelProperties(String url, double minConfidence) {

  public DecisionModelProperties {
    if (url == null || url.isBlank()) {
      url = "http://localhost:8000";
    }
    if (minConfidence <= 0) {
      minConfidence = 0.7;
    }
  }
}
