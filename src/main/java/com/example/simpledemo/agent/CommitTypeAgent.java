package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.decision.CommitTypeClassifier;
import com.example.simpledemo.decision.CommitTypeDecision;

/**
 * Two steps, two kinds of model. The planner chains them: {@code UserInput -> Request ->
 * CommitTypeDecision}.
 *
 * <ol>
 *   <li>{@link #describeChange}: the LLM copies the change out of the user's message. The decision
 *       model reads every word it is given, so "decide the commit type of: handle null user in
 *       login" sent whole comes back {@code test} at 0.83, while "handle null user in login"
 *       comes back {@code fix} at 0.87.
 *   <li>{@link #decide}: the decision model picks one of six fixed types. No LLM.
 * </ol>
 */
@Agent(
    name = "CommitType",
    description =
        "Decides the Conventional Commits type (feat, fix, docs, refactor, test, chore) of a"
            + " described change, using a local decision model")
public class CommitTypeAgent {

  /** The change on its own, without the request around it. */
  public record Request(String change) {}

  private final CommitTypeClassifier classifier;

  public CommitTypeAgent(CommitTypeClassifier classifier) {
    this.classifier = classifier;
  }

  @Action(description = "Copy the description of the change out of the user's message")
  public Request describeChange(UserInput userInput, Ai ai) {
    return ai.withDefaultLlm()
        .createObject(
            """
            The message below asks about a code change. Copy the description of the change,
            word for word, and leave out the request around it.
            For "what type is this commit: add a logout button", the change is "add a logout button".
            Message: %s
            Return JSON with field: change (string).
            """
                .formatted(userInput.getContent()),
            Request.class);
  }

  @Action(canRerun = true, description = "Ask the decision model which commit type the change is")
  @AchievesGoal(description = "Know which Conventional Commits type a change is")
  public CommitTypeDecision decide(Request request) {
    return classifier.classify(request.change());
  }
}
