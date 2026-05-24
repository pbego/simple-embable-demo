package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.Ai;

/**
 * Single-step LLM worker. Picked by Autonomy for joke-related {@code x} input, or invoked from
 * {@link com.example.simpledemo.chat.ChatRouter} during {@code chat}.
 */
@Agent(name = "Joke", description = "Tells a short, family-friendly joke")
public class JokeAgent {

  public record Request(String topic) {}

  @Action(canRerun = true, description = "Tell a one-liner joke")
  @AchievesGoal(description = "Entertain the user with a joke")
  public JokeResult tell(Request request, ActionContext context) {
    return tell(request, context.ai());
  }

  private JokeResult tell(Request request, Ai ai) {
    var topic = request != null && request.topic() != null ? request.topic() : "programming";
    return ai.withDefaultLlm()
        .createObject(
            """
            Tell exactly one short, family-friendly one-liner joke.
            Topic hint from the user (may be empty): %s
            Return JSON with field: joke (string).
            """.formatted(topic),
            JokeResult.class);
  }
}
