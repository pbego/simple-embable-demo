package com.example.simpledemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.tools.ToolishRag;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Agentic RAG over project docs (tutorial 18): the LLM chooses when and how to search.
 */
@Agent(description = "Answer questions about commit conventions and project docs using RAG tools")
public class CommitStyleAgent {

  private final LuceneSearchOperations searchOperations;

  public CommitStyleAgent(ObjectProvider<LuceneSearchOperations> searchOperations) {
    this.searchOperations = searchOperations.getIfAvailable();
  }

  @AchievesGoal(description = "Explain how commits should be formatted in this repository")
  @Action
  public String explainCommitStyle(UserInput userInput, Ai ai) {
    if (searchOperations == null) {
      return "RAG is disabled. Set simple-demo.rag.enabled=true and run rag-index.";
    }
    var question = userInput.getContent() == null ? "" : userInput.getContent().trim();
    var toolishRag =
        new ToolishRag(
            "repoDocs",
            "Project commit conventions, tutorials, and example past commit messages",
            searchOperations);

    return ai.withDefaultLlm()
        .withReference(toolishRag)
        .createObject(
            """
            You help developers follow this repository's commit message conventions.
            Search the indexed documentation before answering. Cite specific rules from retrieved chunks.
            If the index is empty, say to run shell command rag-index first.

            Developer question:
            %s
            """
                .formatted(question.isEmpty() ? "How should we format commit messages here?" : question),
            String.class);
  }
}
