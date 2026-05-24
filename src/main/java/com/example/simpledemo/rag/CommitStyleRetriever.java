package com.example.simpledemo.rag;

import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.model.Chunk;
import com.embabel.common.core.types.SimilarityResult;
import com.embabel.common.core.types.TextSimilaritySearchRequest;
import com.example.simpledemo.agent.GitChanges;
import com.example.simpledemo.config.RagProperties;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * One-shot RAG retrieval for commit message generation (tutorial 17).
 */
@Component
public class CommitStyleRetriever {

  private final ObjectProvider<LuceneSearchOperations> searchOperations;
  private final RagProperties properties;

  public CommitStyleRetriever(
      ObjectProvider<LuceneSearchOperations> searchOperations, RagProperties properties) {
    this.searchOperations = searchOperations;
    this.properties = properties;
  }

  public String retrieveStyleGuide(GitChanges changes) {
    return formatChunks(search(changesQuery(changes)));
  }

  public String retrieveForQuery(String query) {
    return formatChunks(search(query));
  }

  public List<SimilarityResult<Chunk>> search(String query) {
    var store = searchOperations.getIfAvailable();
    if (store == null) {
      return List.of();
    }
    var request =
        TextSimilaritySearchRequest.create(
            query, properties.similarityThreshold(), properties.retrievalTopK());
    return store.vectorSearch(request, Chunk.class);
  }

  private static String changesQuery(GitChanges changes) {
    var status = changes.status() == null ? "" : changes.status();
    var staged = changes.stagedDiff() == null ? "" : changes.stagedDiff();
    var snippet = (status + "\n" + staged).trim();
    if (snippet.length() > 500) {
      snippet = snippet.substring(0, 500);
    }
    if (snippet.isBlank()) {
      return "commit message conventions conventional commits subject body";
    }
    return "commit message conventions for changes:\n" + snippet;
  }

  static String formatChunks(List<SimilarityResult<Chunk>> results) {
    if (results == null || results.isEmpty()) {
      return "";
    }
    return results.stream()
        .map(SimilarityResult::getMatch)
        .map(Chunk::getText)
        .filter(text -> text != null && !text.isBlank())
        .map(text -> "- " + text.trim())
        .collect(Collectors.joining("\n"));
  }
}
