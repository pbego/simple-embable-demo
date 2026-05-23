package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.git.GitExecutor;
import com.example.simpledemo.git.GitRepository;

/**
 * Answers questions about the local repository by giving the LLM {@link GitRepository} tools.
 */
@Agent(description = "Answer questions about the local git repository using git tools")
public class GitInfoAgent {

  private final GitExecutor gitExecutor;

  public GitInfoAgent(GitExecutor gitExecutor) {
    this.gitExecutor = gitExecutor;
  }

  @AchievesGoal(description = "Answer a question about the git repository")
  @Action
  public String answerAboutRepo(UserInput userInput, Ai ai) {
    var repository = new GitRepository(gitExecutor);
    var question = userInput.getContent() == null ? "" : userInput.getContent().trim();

    return ai.withDefaultLlm()
        .withToolObject(repository)
        .createObject(
            """
            You help developers understand their local git repository.
            Use the available git tools to look up facts. Do not invent branch names, commits, or file paths.

            Developer question:
            %s

            Reply in plain text, concise and factual.
            """
                .formatted(question.isEmpty() ? "Summarize the repository state." : question),
            String.class);
  }
}
