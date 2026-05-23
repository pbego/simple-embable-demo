package com.example.simpledemo.config;

import com.embabel.agent.rag.ingestion.ContentChunker;
import com.embabel.agent.rag.ingestion.transform.AddTitlesChunkTransformer;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.embabel.common.ai.model.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "simple-demo.rag.enabled", havingValue = "true", matchIfMissing = true)
public class RagConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(RagConfiguration.class);

  @Bean(destroyMethod = "close")
  LuceneSearchOperations luceneSearchOperations(ModelProvider modelProvider, RagProperties properties) {
    var embeddingService = modelProvider.getEmbeddingService(DefaultModelSelectionCriteria.INSTANCE);
    var chunkerConfig = new ContentChunker.Config(properties.maxChunkSize(), properties.overlapSize(), 32);
    var store =
        LuceneSearchOperations.Companion.withName("commit-docs")
            .withEmbeddingService(embeddingService)
            .withChunkerConfig(chunkerConfig)
            .withChunkTransformer(AddTitlesChunkTransformer.INSTANCE)
            .withIndexPath(properties.indexPath())
            .buildAndLoadChunks();
    logger.info(
        "Lucene RAG store at {} ({} chunks on disk)",
        properties.indexPath(),
        store.info().getChunkCount());
    return store;
  }
}
