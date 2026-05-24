package com.example.simpledemo.shell;

import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.example.simpledemo.rag.CommitCorpusIngester;
import com.example.simpledemo.rag.CommitStyleRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@ConditionalOnBean(LuceneSearchOperations.class)
public class RagShellCommands {

  private final CommitCorpusIngester ingester;
  private final CommitStyleRetriever retriever;

  public RagShellCommands(CommitCorpusIngester ingester, CommitStyleRetriever retriever) {
    this.ingester = ingester;
    this.retriever = retriever;
  }

  @ShellMethod(value = "Rebuild Lucene index from configured commit-style sources", key = "rag-index")
  public String ragIndex() throws Exception {
    ingester.ensureIndexDirectory();
    return ingester.rebuildIndex().format();
  }

  @ShellMethod(value = "Vector search the commit-style index (no LLM)", key = "rag-search")
  public String ragSearch(
      @ShellOption(
              value = {"-q", "--query"},
              defaultValue = "conventional commits subject line",
              help = "Search query")
          String query) {
    return retriever.retrieveForQuery(query);
  }
}
