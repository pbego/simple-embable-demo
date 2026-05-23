package com.example.simpledemo.memory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.simpledemo.agent.CommitMessage;
import com.example.simpledemo.config.VectorMemoryProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class CommitVectorMemoryTest {

  @Test
  void recallReturnsEmptyWhenInactive() {
    var properties = new VectorMemoryProperties(false, null, 3);
    ObjectProvider<org.springframework.ai.vectorstore.SimpleVectorStore> provider =
        new ObjectProvider<>() {
          @Override
          public org.springframework.ai.vectorstore.SimpleVectorStore getObject() {
            return null;
          }

          @Override
          public org.springframework.ai.vectorstore.SimpleVectorStore getObject(
              Object... args) {
            return null;
          }

          @Override
          public org.springframework.ai.vectorstore.SimpleVectorStore getIfAvailable() {
            return null;
          }

          @Override
          public org.springframework.ai.vectorstore.SimpleVectorStore getIfUnique() {
            return null;
          }
        };
    var memory = new CommitVectorMemory(provider, properties);
    assertTrue(memory.recallSimilar("feat: add RAG").isEmpty());
    memory.remember(new CommitMessage("feat: test", "body"), "main");
    assertFalse(memory.isActive());
  }
}
