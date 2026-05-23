package com.example.simpledemo.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simple-demo.vector-memory")
public record VectorMemoryProperties(boolean enabled, Path storageFile, int recallTopK) {

  public VectorMemoryProperties {
    if (storageFile == null) {
      storageFile = Path.of(System.getProperty("user.home"), ".simple-demo", "vector-memory.json");
    }
    if (recallTopK <= 0) {
      recallTopK = 3;
    }
  }
}
