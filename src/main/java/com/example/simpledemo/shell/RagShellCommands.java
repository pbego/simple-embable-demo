package com.example.simpledemo.shell;

import com.example.simpledemo.rag.CommitCorpusIngester;
import com.example.simpledemo.rag.CommitStyleRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;

@ShellComponent
@ConditionalOnBean(LuceneSearchOperations.class)
public class RagShellCommands {

  private final CommitCorpusIngester ingester;
  private final CommitStyleRetriever retriever;

  public RagShellCommands(CommitCorpusIngester ingester, CommitStyleRetriever retriever) {
    this.ingester = ingester;
    this.retriever = retriever;
  }

  @Command(command = "rag-index", description = "Rebuild Lucene index from configured commit-style sources")
  public String ragIndex() throws Exception {
    ingester.ensureIndexDirectory();
    return ingester.rebuildIndex().format();
  }

  @Command(command = "rag-search", description = "Vector search the commit-style index (no LLM)")
  public String ragSearch(
      @Option(
              longNames = "query",
              shortNames = 'q',
              defaultValue = "conventional commits subject line",
              description = "Search query")
          String query) {
    return retriever.retrieveForQuery(query);
  }
}
