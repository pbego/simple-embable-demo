package com.example.simpledemo.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** No decision model needed: a mock server plays Von's part. */
class CommitTypeClassifierTest {

  private static final String URL = "http://von.test";

  private MockRestServiceServer server;
  private CommitTypeClassifier classifier;

  @BeforeEach
  void setUp() {
    var builder = RestClient.builder().baseUrl(URL);
    server = MockRestServiceServer.bindTo(builder).build();
    var client = new DecisionModelClient(builder.build());
    classifier = new CommitTypeClassifier(client, new DecisionModelProperties(URL, 0.7));
  }

  @Test
  void sendsTheChangeAndTheSixTypesAsOneChoiceQuestion() {
    server
        .expect(requestTo(URL + "/v1/systemone"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(jsonPath("$.state.change").value("handle null user in login"))
        .andExpect(jsonPath("$.questions.q.type").value("choice"))
        .andExpect(jsonPath("$.questions.q.instructions").value(CommitTypeClassifier.QUESTION))
        .andExpect(
            jsonPath("$.questions.q.criteria.fix").value(CommitTypeClassifier.TYPES.get("fix")))
        .andExpect(jsonPath("$.questions.q.criteria.length()").value(6))
        .andRespond(
            withSuccess(answer("fix", 0.87, "feat", 0.08, "test", 0.05), MediaType.APPLICATION_JSON));

    var decision = classifier.classify("  handle null user in login ");

    server.verify();
    assertEquals("handle null user in login", decision.change());
    assertEquals("fix", decision.type());
    assertEquals(0.87, decision.probability());
    assertTrue(decision.confident());
    assertTrue(decision.formatted().startsWith("fix (0.87): confident"));
  }

  @Test
  void aProbabilityBelowTheMinimumIsReportedAsUnsure() {
    server
        .expect(requestTo(URL + "/v1/systemone"))
        .andRespond(
            withSuccess(
                answer("feat", 0.55, "test", 0.40, "chore", 0.05), MediaType.APPLICATION_JSON));

    var decision = classifier.classify("add unit tests for the price calculator");

    assertEquals("feat", decision.type());
    assertFalse(decision.confident());
    assertTrue(decision.formatted().startsWith("unsure (0.55): the model leans to 'feat'"));
  }

  @Test
  void aBlankChangeIsRefusedBeforeAskingTheModel() {
    assertThrows(IllegalArgumentException.class, () -> classifier.classify(" "));
    server.verify();
  }

  /** A Von answer, with the extra fields it really sends, for one question named {@code q}. */
  private static String answer(
      String choice, double p, String second, double p2, String third, double p3) {
    return """
        {"model": "von-1.3.0",
         "answers": {"q": {"type": "choice", "choice": "%s",
                           "probabilities": {"%s": %s, "%s": %s, "%s": %s},
                           "confidence": 0.5}},
         "usage": {"input_tokens": 28, "output_tokens": 1}}
        """
        .formatted(choice, choice, p, second, p2, third, p3);
  }
}
