package com.example.simpledemo.memory;

import com.example.simpledemo.agent.CommitMessage;
import com.example.simpledemo.config.VectorMemoryProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Long-term semantic memory of past commit suggestions (tutorial 20).
 */
@Component
public class CommitVectorMemory {

  private static final Logger logger = LoggerFactory.getLogger(CommitVectorMemory.class);

  private final SimpleVectorStore vectorStore;
  private final VectorMemoryProperties properties;

  public CommitVectorMemory(
      ObjectProvider<SimpleVectorStore> vectorStore, VectorMemoryProperties properties) {
    this.vectorStore = vectorStore.getIfAvailable();
    this.properties = properties;
  }

  public boolean isActive() {
    return properties.enabled() && vectorStore != null;
  }

  public void remember(CommitMessage commit, String repoId) {
    if (!isActive() || commit == null || commit.subject() == null || commit.subject().isBlank()) {
      return;
    }
    var text = commit.formatted();
    var document =
        new Document(
            text,
            Map.of(
                "type", "commit-suggestion",
                "repoId", repoId == null ? "default" : repoId,
                "subject", commit.subject()));
    vectorStore.add(List.of(document));
    persist();
    logger.debug("Remembered commit suggestion: {}", commit.subject());
  }

  public String recallSimilar(String query) {
    if (!isActive()) {
      return "";
    }
    var searchQuery = query == null || query.isBlank() ? "recent commit message" : query;
    var request =
        SearchRequest.builder().query(searchQuery).topK(properties.recallTopK()).build();
    var hits = vectorStore.similaritySearch(request);
    if (hits.isEmpty()) {
      return "";
    }
    return hits.stream()
        .map(Document::getText)
        .filter(text -> text != null && !text.isBlank())
        .map(text -> "- " + text.trim())
        .collect(Collectors.joining("\n"));
  }

  private void persist() {
    try {
      vectorStore.save(properties.storageFile().toFile());
    } catch (RuntimeException e) {
      logger.warn("Could not persist commit vector memory: {}", e.getMessage());
    }
  }
}
