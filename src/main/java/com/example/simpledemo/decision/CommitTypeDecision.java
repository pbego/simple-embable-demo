package com.example.simpledemo.decision;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The decision model's answer to "which Conventional Commits type is this change?".
 *
 * @param change the description that was asked about
 * @param type the type the model chose
 * @param probability the chosen type's probability
 * @param confident whether that probability reached the configured minimum; when it did not, do
 *     not use {@code type} as it is
 * @param probabilities every type's probability
 */
public record CommitTypeDecision(
    String change,
    String type,
    double probability,
    boolean confident,
    Map<String, Double> probabilities) {

  /** Shell-friendly text: the verdict, then every type from most to least likely. */
  public String formatted() {
    var verdict =
        confident
            ? String.format(Locale.ROOT, "%s (%.2f): confident, use it", type, probability)
            : String.format(
                Locale.ROOT,
                "unsure (%.2f): the model leans to '%s'. Pick the type yourself, or ask an LLM.",
                probability,
                type);
    var table =
        probabilities.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
            .map(e -> String.format(Locale.ROOT, "  %-9s %.2f", e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n"));
    return verdict + "\n" + table;
  }
}
