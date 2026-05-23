package com.example.simpledemo.chat;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.EmbabelComponent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Conversation;
import com.embabel.chat.UserMessage;
import com.example.simpledemo.agent.CommitMessage;
import com.example.simpledemo.agent.CommitMessageAgent;
import com.example.simpledemo.agent.CommitStyleAgent;
import com.example.simpledemo.agent.GreetingAgent;
import com.example.simpledemo.agent.JokeAgent;
import com.example.simpledemo.agent.JokeResult;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chat entry point (like IAX {@code Router}): runs on every {@link UserMessage} in a {@code chat}
 * session and dispatches to a specialist sub-agent.
 *
 * <p>Contrast with shell {@code x}: Autonomy picks a whole {@code @Agent} and the planner may run
 * multiple {@code @Action} steps (see {@link com.example.simpledemo.agent.CommitMessageAgent}).
 */
@EmbabelComponent
public class ChatRouter {

  private static final Logger logger = LoggerFactory.getLogger(ChatRouter.class);

  private final GreetingAgent greetingAgent;
  private final JokeAgent jokeAgent;
  private final CommitMessageAgent commitMessageAgent;
  private final CommitStyleAgent commitStyleAgent;

  public ChatRouter(
      GreetingAgent greetingAgent,
      JokeAgent jokeAgent,
      CommitMessageAgent commitMessageAgent,
      CommitStyleAgent commitStyleAgent) {
    this.greetingAgent = greetingAgent;
    this.jokeAgent = jokeAgent;
    this.commitMessageAgent = commitMessageAgent;
    this.commitStyleAgent = commitStyleAgent;
  }

  @Action(canRerun = true, trigger = UserMessage.class)
  public void respond(Conversation conversation, ActionContext context) {
    var lastUserMessage = conversation.lastMessageIfBeFromUser();
    if (lastUserMessage == null) {
      logger.debug("Skipping non-user message");
      return;
    }

    var question = lastUserMessage.getContent();
    var route = fromMessage(question);
    logger.info("ChatRouter route={} for message={}", route, question);

    String response =
        switch (route) {
          case JOKE -> formatJoke(jokeAgent.tell(new JokeAgent.Request(question), context.ai()));
          case COMMIT -> suggestCommitMessage(question, context);
          case STYLE -> explainCommitStyle(question, context);
          case GREETING -> greetingAgent.greet(new GreetingAgent.Request(question));
        };

    context.sendMessage(conversation.addMessage(new AssistantMessage(response)));
  }

  /**
   * Keyword routing for a predictable demo (IAX uses an LLM + {@code Subagent} tools for SRE mode).
   */
  static RouteTarget fromMessage(String message) {
    var text = message != null ? message.toLowerCase(Locale.ROOT) : "";
    if (text.contains("joke") || text.contains("funny")) {
      return RouteTarget.JOKE;
    }
    if (isStyleQuestion(text)) {
      return RouteTarget.STYLE;
    }
    if (text.contains("commit") || text.contains("git")) {
      return RouteTarget.COMMIT;
    }
    return RouteTarget.GREETING;
  }

  private static boolean isStyleQuestion(String text) {
    return text.contains("convention")
        || text.contains("conventional")
        || (text.contains("format") && text.contains("commit"))
        || text.contains("style guide")
        || (text.contains("how") && text.contains("commit") && !text.contains("generate"));
  }

  private static String formatJoke(JokeResult result) {
    if (result == null || result.joke() == null || result.joke().isBlank()) {
      return "I couldn't think of a joke right now.";
    }
    return result.joke().trim();
  }

  private String explainCommitStyle(String question, ActionContext context) {
    var userInput = new UserInput(question != null ? question : "");
    var answer = commitStyleAgent.explainCommitStyle(userInput, context.ai());
    return answer == null || answer.isBlank()
        ? "Run rag-index first, then ask about commit conventions."
        : answer.trim();
  }

  private String suggestCommitMessage(String question, ActionContext context) {
    var userInput = new UserInput(question != null ? question : "");
    var changes = commitMessageAgent.collectChanges(userInput);
    var commit = commitMessageAgent.generateCommitMessage(changes, userInput, context.ai());
    return formatCommitMessage(commit);
  }

  private static String formatCommitMessage(CommitMessage commit) {
    if (commit == null) {
      return "I could not generate a commit message right now.";
    }
    return commit.formatted();
  }

  enum RouteTarget {
    GREETING,
    JOKE,
    COMMIT,
    STYLE
  }
}
