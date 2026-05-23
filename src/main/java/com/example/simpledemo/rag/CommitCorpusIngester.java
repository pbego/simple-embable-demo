package com.example.simpledemo.rag;

import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import com.embabel.agent.rag.ingestion.policy.AlwaysRefreshContentRefreshPolicy;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.example.simpledemo.config.RagProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Indexes markdown/text sources into the Lucene RAG store (tutorial 17).
 */
@Component
@ConditionalOnBean(LuceneSearchOperations.class)
public class CommitCorpusIngester {

  private static final Logger logger = LoggerFactory.getLogger(CommitCorpusIngester.class);

  private final LuceneSearchOperations searchOperations;
  private final RagProperties properties;
  private final TikaHierarchicalContentReader contentReader = new TikaHierarchicalContentReader();

  public CommitCorpusIngester(LuceneSearchOperations searchOperations, RagProperties properties) {
    this.searchOperations = searchOperations;
    this.properties = properties;
  }

  public IngestSummary rebuildIndex() {
    var cleared = searchOperations.clear();
    logger.info("Cleared {} chunks from Lucene index", cleared);
  var ingested = new ArrayList<String>();
    var skipped = new ArrayList<String>();
    for (var source : properties.sources()) {
      var path = Path.of(source);
      if (!Files.isRegularFile(path)) {
        skipped.add(source + " (not found)");
        continue;
      }
      var uri = path.toAbsolutePath().toUri().toString();
      var document =
          AlwaysRefreshContentRefreshPolicy.INSTANCE.ingestUriIfNeeded(
              searchOperations, contentReader, uri);
      if (document != null) {
        ingested.add(source);
      } else {
        skipped.add(source + " (not ingested)");
      }
    }
    return new IngestSummary(ingested, skipped, searchOperations.info().getChunkCount());
  }

  public record IngestSummary(List<String> ingested, List<String> skipped, int chunkCount) {

    public String format() {
      var lines = new ArrayList<String>();
      lines.add("Indexed %d chunks.".formatted(chunkCount));
      if (!ingested.isEmpty()) {
        lines.add("Ingested: " + String.join(", ", ingested));
      }
      if (!skipped.isEmpty()) {
        lines.add("Skipped: " + String.join(", ", skipped));
      }
      return String.join(System.lineSeparator(), lines);
    }
  }

  public void ensureIndexDirectory() throws IOException {
    Files.createDirectories(properties.indexPath());
  }
}
