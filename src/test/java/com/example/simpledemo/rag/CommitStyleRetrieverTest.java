package com.example.simpledemo.rag;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.rag.model.Chunk;
import com.embabel.common.core.types.SimilarityResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommitStyleRetrieverTest {

  @Test
  void formatChunksJoinsRetrievedText() {
    var chunk = mock(Chunk.class);
    when(chunk.getText()).thenReturn("Use Conventional Commits for subjects.");
    var results = List.of(SimilarityResult.create(chunk, 0.9));
    var formatted = CommitStyleRetriever.formatChunks(results);
    assertTrue(formatted.contains("Conventional Commits"));
  }

  @Test
  void formatChunksReturnsEmptyWhenNoResults() {
    assertTrue(CommitStyleRetriever.formatChunks(List.of()).isEmpty());
  }
}
