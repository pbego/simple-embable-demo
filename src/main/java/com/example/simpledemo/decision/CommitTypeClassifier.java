package com.example.simpledemo.decision;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Picks the Conventional Commits type of a change with a decision model instead of an LLM.
 *
 * <p>The types are the six in {@code docs/COMMIT_CONVENTIONS.md}. Each gets one sentence saying
 * what it means; the model compares the change against those sentences, so the wording of
 * {@link #TYPES} is the whole "prompt". Change a sentence and the answers change.
 */
@Component
public class CommitTypeClassifier {

  static final String QUESTION = "What kind of change does this commit make?";

  static final Map<String, String> TYPES = types();

  private static Map<String, String> types() {
    var types = new LinkedHashMap<String, String>();
    types.put("feat", "A new feature: something users can do that they could not do before");
    types.put("fix", "A bug fix: something that was broken now works as intended");
    types.put("docs", "Documentation only: README, guides, javadoc or comments");
    types.put("refactor", "Restructures or renames code without changing what it does");
    types.put("test", "Adds or changes tests only");
    types.put(
        "chore", "Build, dependencies, CI or tooling; the application itself does not change");
    return Collections.unmodifiableMap(types);
  }

  private final DecisionModelClient client;
  private final double minConfidence;

  public CommitTypeClassifier(DecisionModelClient client, DecisionModelProperties properties) {
    this.client = client;
    this.minConfidence = properties.minConfidence();
  }

  /**
   * @param change a one-line description of the change, e.g. "handle null user in login"
   */
  public CommitTypeDecision classify(String change) {
    if (change == null || change.isBlank()) {
      throw new IllegalArgumentException("Describe the change, e.g. \"handle null user in login\"");
    }
    var decision = client.choose(Map.of("change", change.trim()), QUESTION, TYPES);
    return new CommitTypeDecision(
        change.trim(),
        decision.choice(),
        decision.probability(),
        decision.probability() >= minConfidence,
        decision.probabilities());
  }
}
