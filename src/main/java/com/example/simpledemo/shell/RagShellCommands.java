package com.example.simpledemo.shell;

import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.example.simpledemo.rag.CommitCorpusIngester;
import com.example.simpledemo.rag.CommitStyleRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(LuceneSearchOperations.class)
public class RagShellCommands {

  private final CommitCorpusIngester ingester;
  private final CommitStyleRetriever retriever;

  public RagShellCommands(CommitCorpusIngester ingester, CommitStyleRetriever retriever) {
    this.ingester = ingester;
    this.retriever = retriever;
  }

  @Command(name = "rag-index", description = "Rebuild Lucene index from configured commit-style sources")
  public String ragIndex() throws Exception {
    ingester.ensureIndexDirectory();
    return ingester.rebuildIndex().format();
  }

  @Command(name = "rag-search", description = "Vector search the commit-style index (no LLM)")
  public String ragSearch(
      @Option(
              longName = "query",
              shortName = 'q',
              defaultValue = "conventional commits subject line",
              description = "Search query")
          String query) {
    return retriever.retrieveForQuery(query);
  }
}
