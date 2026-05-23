package com.example.simpledemo.config;

import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "simple-demo.vector-memory.enabled", havingValue = "true")
@ConditionalOnBean(EmbeddingModel.class)
public class VectorMemoryConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(VectorMemoryConfiguration.class);

  @Bean(destroyMethod = "")
  SimpleVectorStore commitVectorStore(EmbeddingModel embeddingModel, VectorMemoryProperties properties)
      throws IOException {
    var store = SimpleVectorStore.builder(embeddingModel).build();
    var file = properties.storageFile().toFile();
    if (file.isFile()) {
      store.load(file);
      logger.info("Loaded commit vector memory from {}", file.getAbsolutePath());
    } else {
      Files.createDirectories(file.getParentFile().toPath());
    }
    return store;
  }
}
