package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * Minimal Embabel agent: extracts a topic from user input, then summarizes it.
 * Runs entirely against the configured local Ollama model.
 */
@Agent(description = "Extract a topic from user input and summarize it")
public class TopicSummarizerAgent {

  @Action
  public Topic extractTopic(UserInput userInput, Ai ai) {
    return ai.withDefaultLlm()
        .createObject("""
            Extract a short topic title from this user message:
            %s
            """.formatted(userInput.getContent()), Topic.class);
  }

  @AchievesGoal(description = "Produce a clear summary of the topic")
  @Action
  public Summary summarize(Topic topic, Ai ai) {
    return ai.withDefaultLlm()
        .createObject("""
            Write a concise, friendly summary (3-5 paragraphs) about: %s
            """.formatted(topic.title()), Summary.class);
  }
}
