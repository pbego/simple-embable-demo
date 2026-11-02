package com.example.simpledemo.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Asks a decision model one multiple-choice question.
 *
 * <p>A decision model is a small classifier, not a chat model: you give it some facts (the
 * {@code state}), a question and a fixed set of options, each described in one sentence. It
 * returns one of those options and a probability for every option, in a single forward pass on
 * CPU. It never writes text, so its answer can always be used in code as it is.
 *
 * <p>The wire format is the one Von and Laya serve:
 *
 * <pre>
 * POST /v1/systemone
 * {"state": {...}, "questions": {"q": {"type": "choice", "instructions": "...", "criteria": {...}}}}
 * </pre>
 */
public class DecisionModelClient {

  private static final String QUESTION_ID = "q";

  private final RestClient restClient;

  public DecisionModelClient(RestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * One answer: the option the model chose, and its probability for every option.
   */
  public record Decision(String choice, Map<String, Double> probabilities) {

    /** The probability of the chosen option, from 0 to 1. */
    public double probability() {
      return probabilities.getOrDefault(choice, 0.0);
    }
  }

  /**
   * @param state the facts the question is about, sent as JSON
   * @param instructions the question, in one sentence
   * @param options option id to a one-sentence description; the model returns one of the ids
   */
  public Decision choose(Map<String, ?> state, String instructions, Map<String, String> options) {
    var question = Map.of("type", "choice", "instructions", instructions, "criteria", options);
    var body = Map.of("state", state, "questions", Map.of(QUESTION_ID, question));
    var response =
        restClient
            .post()
            .uri("/v1/systemone")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(Response.class);
    if (response == null
        || response.answers() == null
        || !response.answers().containsKey(QUESTION_ID)) {
      throw new IllegalStateException("The decision model returned no answer");
    }
    var answer = response.answers().get(QUESTION_ID);
    return new Decision(answer.choice(), answer.probabilities());
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Response(Map<String, Answer> answers) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Answer(String choice, Map<String, Double> probabilities) {}
}
