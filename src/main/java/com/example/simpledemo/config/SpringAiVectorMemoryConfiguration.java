package com.example.simpledemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Optional Spring AI parallel to Embabel file chat memory (tutorial 20).
 *
 * <p>{@code VectorStoreChatMemoryAdvisor} lives in {@code spring-ai-advisors-vector-store}
 * (newer Spring AI). This profile documents the pattern without pulling a second BOM version.
 * When you add that dependency, wire a {@code ChatClient} like:
 *
 * <pre>{@code
 * ChatClient.builder(chatModel)
 *     .defaultAdvisors(
 *         VectorStoreChatMemoryAdvisor.builder(commitVectorStore)
 *             .conversationId("demo")
 *             .build())
 *     .build();
 * }</pre>
 *
 * <p>Embabel {@code chat} stays the primary shell path; use this for experiments comparing
 * transcript memory vs vector recall.
 */
@Configuration
@Profile("spring-ai-memory")
@ConditionalOnProperty(name = "simple-demo.vector-memory.enabled", havingValue = "true")
public class SpringAiVectorMemoryConfiguration {
  // Intentionally empty: see class Javadoc and TUTORIAL-VECTOR-MEMORY.md.
}
