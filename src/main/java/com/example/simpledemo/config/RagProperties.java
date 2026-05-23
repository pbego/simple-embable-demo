package com.example.simpledemo.config;

import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simple-demo.rag")
public record RagProperties(
    Path indexPath,
    List<String> sources,
    int maxChunkSize,
    int overlapSize,
    int retrievalTopK,
    double similarityThreshold) {

  public RagProperties {
    if (indexPath == null) {
      indexPath = Path.of(System.getProperty("user.home"), ".simple-demo", "lucene-index");
    }
    if (sources == null || sources.isEmpty()) {
      sources =
          List.of(
              "docs/COMMIT_CONVENTIONS.md",
              "TUTORIAL.md",
              "rag-sources/past-commits.sample.txt");
    }
    if (maxChunkSize <= 0) {
      maxChunkSize = 800;
    }
    if (overlapSize < 0) {
      overlapSize = 100;
    }
    if (retrievalTopK <= 0) {
      retrievalTopK = 3;
    }
    if (similarityThreshold < 0) {
      similarityThreshold = 0.0;
    }
  }
}
