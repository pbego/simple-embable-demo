package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;

/**
 * Deterministic worker: no LLM. Useful to see {@code x} pick a non-LLM agent via Autonomy.
 */
@Agent(name = "Greeting", description = "Greets the user and echoes their message")
public class GreetingAgent {

  public record Request(String message) {}

  @Action(canRerun = true, description = "Return a friendly greeting")
  @AchievesGoal(description = "Greet the user")
  public String greet(Request request) {
    var message = request != null && request.message() != null ? request.message() : "";
    if (message.isBlank()) {
      return "Hello! Ask me for a joke, or use the chat command for a routed conversation.";
    }
    return "Hello! You said: \"%s\"".formatted(message.trim());
  }
}
