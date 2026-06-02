package com.example.simpledemo.chat;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.EmbabelComponent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Conversation;
import com.embabel.chat.UserMessage;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.agent.CommitMessage;
import com.example.simpledemo.agent.CommitMessageAgent;
import com.example.simpledemo.agent.CommitOrchestratorAgent;
import com.example.simpledemo.agent.CommitStyleAgent;
import com.example.simpledemo.agent.GreetingAgent;
import com.example.simpledemo.agent.JokeAgent;
import com.example.simpledemo.agent.JokeResult;
import com.example.simpledemo.audit.AuditRecorder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chat entry point: every {@link UserMessage} is routed to one or more specialist {@code @Agent}s.
 *
 * <ul>
 *   <li><b>Explicit agent prefix</b> — {@code @commit ...}, {@code @style ...}, {@code @joke ...},
 *       {@code @greet ...} (single worker; no routing LLM).
 *   <li><b>Natural language</b> — routing LLM returns {@code targets} (can be more than one); keyword
 *       hints augment the list when the user asks for multiple things in one message.
 * </ul>
 */
@EmbabelComponent
public class ChatRouter {

  private static final Logger logger = LoggerFactory.getLogger(ChatRouter.class);

  private static final List<RouteTarget> DISPATCH_ORDER =
      List.of(
          RouteTarget.ORCHESTRATE,
          RouteTarget.COMMIT,
          RouteTarget.STYLE,
          RouteTarget.JOKE,
          RouteTarget.GREETING);

  private final GreetingAgent greetingAgent;
  private final JokeAgent jokeAgent;
  private final CommitMessageAgent commitMessageAgent;
  private final CommitStyleAgent commitStyleAgent;
  private final CommitOrchestratorAgent commitOrchestratorAgent;
  private final SessionSummaryService sessionSummaryService;
  private final ChatContextService chatContextService;
  private final AuditRecorder auditRecorder;

  public ChatRouter(
      GreetingAgent greetingAgent,
      JokeAgent jokeAgent,
      CommitMessageAgent commitMessageAgent,
      CommitStyleAgent commitStyleAgent,
      CommitOrchestratorAgent commitOrchestratorAgent,
      SessionSummaryService sessionSummaryService,
      ChatContextService chatContextService,
      AuditRecorder auditRecorder) {
    this.greetingAgent = greetingAgent;
    this.jokeAgent = jokeAgent;
    this.commitMessageAgent = commitMessageAgent;
    this.commitStyleAgent = commitStyleAgent;
    this.commitOrchestratorAgent = commitOrchestratorAgent;
    this.sessionSummaryService = sessionSummaryService;
    this.chatContextService = chatContextService;
    this.auditRecorder = auditRecorder;
  }

  @Action(canRerun = true, trigger = UserMessage.class)
  public void respond(Conversation conversation, ActionContext context) {
    var lastUserMessage = conversation.lastMessageIfBeFromUser();
    if (lastUserMessage == null) {
      logger.debug("Skipping non-user message");
      return;
    }

    sessionSummaryService.refreshSummaryIfNeeded(conversation, context);

    var raw = lastUserMessage.getContent();
    var parsed = parseMessage(raw);
    List<RouteTarget> routes;
    String rationale = "explicit prefix";
    if (parsed.explicitTarget().isPresent()) {
      routes = List.of(parsed.explicitTarget().orElseThrow());
    } else {
      var decision = routeViaLlm(conversation, parsed.question(), context);
      routes = decision.targets();
      rationale = decision.rationale();
      logger.info("ChatRouter routes={} rationale={}", routes, rationale);
    }
    auditRecorder.routerDecision(
        conversation.getId(),
        routes.stream().map(Enum::name).toList(),
        rationale,
        parsed.explicitTarget().isPresent());

    var question = parsed.question();
    logger.info("ChatRouter question={}", question);

    var contextualQuestion = chatContextService.enrichQuestion(conversation, question);
    var response = dispatchAll(conversation.getId(), routes, contextualQuestion, context);
    context.sendMessage(conversation.addMessage(new AssistantMessage(response)));
  }

  /**
   * Optional {@code @agent} prefix. When present, skips the routing LLM.
   *
   * <p>Examples: {@code @commit message for my changes}, {@code @style conventions}, {@code @joke about java}.
   */
  static ParsedMessage parseMessage(String message) {
    var raw = message != null ? message.trim() : "";
    if (!raw.startsWith("@")) {
      return new ParsedMessage(Optional.empty(), raw);
    }
    var space = raw.indexOf(' ');
    var tag = (space < 0 ? raw.substring(1) : raw.substring(1, space)).toLowerCase(Locale.ROOT);
    var rest = space < 0 ? "" : raw.substring(space + 1).trim();
    return new ParsedMessage(targetFromTag(tag), rest);
  }

  static Optional<RouteTarget> targetFromTag(String tag) {
    if (tag == null || tag.isBlank()) {
      return Optional.empty();
    }
    return switch (tag) {
      case "commit", "git", "cm" -> Optional.of(RouteTarget.COMMIT);
      case "orchestrate", "pipeline" -> Optional.of(RouteTarget.ORCHESTRATE);
      case "style", "rag", "conventions" -> Optional.of(RouteTarget.STYLE);
      case "joke", "jokes", "funny" -> Optional.of(RouteTarget.JOKE);
      case "greet", "greeting", "hello", "hi" -> Optional.of(RouteTarget.GREETING);
      default -> Optional.empty();
    };
  }

  private RoutingDecision routeViaLlm(Conversation conversation, String question, ActionContext context) {
    try {
      var messages = chatContextService.routingMessages(conversation);
      var llmDecision =
          context.ai().withDefaultLlm().createObject(messages, LlmRoutingDecision.class);

      return toDecision(llmDecision, question);
    } catch (Exception e) {
      logger.warn("Routing LLM failed, defaulting to greet", e);
      return new RoutingDecision(
          List.of(RouteTarget.GREETING), "router_error: " + e.getClass().getSimpleName());
    }
  }

  static RoutingDecision toDecision(LlmRoutingDecision llm, String question) {
    var rationale = llm != null && llm.rationale() != null ? llm.rationale() : "none";
    var targets = resolveTargets(llm, question);
    return new RoutingDecision(targets, rationale);
  }

  /** Visible for tests. */
  static List<RouteTarget> resolveTargets(LlmRoutingDecision llm, String question) {
    var fromLlm = targetsFromLlm(llm);
    var inferred = inferTargetsFromMessage(question);
    if (inferred.size() >= 2) {
      var merged = new LinkedHashSet<RouteTarget>();
      merged.addAll(fromLlm);
      merged.addAll(inferred);
      return orderTargets(merged);
    }
    if (!fromLlm.isEmpty()) {
      return fromLlm;
    }
    if (!inferred.isEmpty()) {
      return inferred;
    }
    return List.of(RouteTarget.GREETING);
  }

  private static List<RouteTarget> targetsFromLlm(LlmRoutingDecision llm) {
    var raw = new ArrayList<String>();
    if (llm == null) {
      return List.of();
    }
    if (llm.targets() != null) {
      raw.addAll(llm.targets());
    }
    if (raw.isEmpty() && llm.target() != null && !llm.target().isBlank()) {
      raw.add(llm.target());
    }
    var normalized = new LinkedHashSet<RouteTarget>();
    for (var entry : raw) {
      normalized.add(normalizeTarget(entry));
    }
    return orderTargets(normalized);
  }

  static List<RouteTarget> inferTargetsFromMessage(String message) {
    var text = message != null ? message.toLowerCase(Locale.ROOT) : "";
    var targets = new LinkedHashSet<RouteTarget>();
    if (isStyleQuestion(text)) {
      targets.add(RouteTarget.STYLE);
    } else if (mentionsCommit(text)) {
      targets.add(RouteTarget.COMMIT);
    }
    if (mentionsJoke(text)) {
      targets.add(RouteTarget.JOKE);
    }
    if (targets.isEmpty() && mentionsGreeting(text)) {
      targets.add(RouteTarget.GREETING);
    }
    return orderTargets(targets);
  }

  static boolean isStyleQuestion(String text) {
    return text.contains("convention")
        || text.contains("conventional")
        || (text.contains("format") && text.contains("commit"))
        || text.contains("style guide")
        || (text.contains("how") && text.contains("commit") && !text.contains("generate"));
  }

  private static boolean mentionsCommit(String text) {
    return text.contains("commit")
        || text.contains("git ")
        || text.startsWith("git")
        || text.contains("diff")
        || text.contains("staged");
  }

  private static boolean mentionsJoke(String text) {
    return text.contains("joke") || text.contains("funny") || text.contains("humor");
  }

  private static boolean mentionsGreeting(String text) {
    return text.contains("hello") || text.contains("hi ") || text.startsWith("hi");
  }

  private static List<RouteTarget> orderTargets(LinkedHashSet<RouteTarget> targets) {
    var ordered = new ArrayList<RouteTarget>();
    for (var candidate : DISPATCH_ORDER) {
      if (targets.contains(candidate)) {
        ordered.add(candidate);
      }
    }
    return ordered;
  }

  static RouteTarget normalizeTarget(String raw) {
    if (raw == null) {
      return RouteTarget.GREETING;
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "joke", "jokes", "funny" -> RouteTarget.JOKE;
      case "commit", "git", "cm" -> RouteTarget.COMMIT;
      case "orchestrate", "pipeline" -> RouteTarget.ORCHESTRATE;
      case "style", "rag", "conventions" -> RouteTarget.STYLE;
      case "greet", "greeting", "hello", "hi" -> RouteTarget.GREETING;
      default -> RouteTarget.GREETING;
    };
  }

  private String dispatchAll(
      String conversationId, List<RouteTarget> routes, String question, ActionContext context) {
    if (routes == null || routes.isEmpty()) {
      auditRecorder.agentInvoked(conversationId, RouteTarget.GREETING.name(), question);
      return greetingAgent.greet(new GreetingAgent.Request(question != null ? question : ""));
    }
    if (routes.size() == 1) {
      return dispatch(conversationId, routes.getFirst(), question, context);
    }
    var sections = new ArrayList<String>();
    for (var route : routes) {
      sections.add(sectionLabel(route) + "\n" + dispatch(conversationId, route, question, context));
    }
    return String.join("\n\n", sections);
  }

  private static String sectionLabel(RouteTarget route) {
    return switch (route) {
      case COMMIT -> "**Commit message**";
      case ORCHESTRATE -> "**Orchestrated commit**";
      case STYLE -> "**Commit style**";
      case JOKE -> "**Joke**";
      case GREETING -> "**Greeting**";
    };
  }

  private String dispatch(
      String conversationId, RouteTarget route, String question, ActionContext context) {
    var q = question != null ? question : "";
    auditRecorder.agentInvoked(conversationId, route.name(), q);
    return switch (route) {
      case JOKE -> formatJoke(jokeAgent.tell(new JokeAgent.Request(q), context));
      case COMMIT ->
          formatCommitMessage(commitMessageAgent.answer(new CommitMessageAgent.Request(q), context));
      case ORCHESTRATE -> orchestrateCommit(q, context);
      case STYLE -> explainCommitStyle(q, context);
      case GREETING -> greetingAgent.greet(new GreetingAgent.Request(q));
    };
  }

  private String orchestrateCommit(String question, ActionContext context) {
    var userInput = new UserInput(question);
    var ctx = commitOrchestratorAgent.collect(userInput);
    ctx = commitOrchestratorAgent.withSecurityReview(ctx, context.ai());
    ctx = commitOrchestratorAgent.withChangelog(ctx, context.ai());
    var proposal = commitOrchestratorAgent.proposeWithContext(ctx, context.ai());
    var header =
        """
        Security: %s

        Changelog:
        %s

        Proposed commit:
        """
            .formatted(ctx.securityReview(), ctx.changelog());
    return header + proposal.formatted();
  }

  private String explainCommitStyle(String question, ActionContext context) {
    var userInput = new UserInput(question);
    var answer = commitStyleAgent.explainCommitStyle(userInput, context.ai());
    return answer == null || answer.isBlank()
        ? "Run rag-index first, then ask about commit conventions."
        : answer.trim();
  }

  private static String formatJoke(JokeResult result) {
    if (result == null || result.joke() == null || result.joke().isBlank()) {
      return "I couldn't think of a joke right now.";
    }
    return result.joke().trim();
  }

  private static String formatCommitMessage(CommitMessage commit) {
    if (commit == null) {
      return "I could not generate a commit message right now.";
    }
    return commit.formatted();
  }

  record ParsedMessage(Optional<RouteTarget> explicitTarget, String question) {}

  record RoutingDecision(List<RouteTarget> targets, String rationale) {}

  /**
   * JSON from the routing LLM. {@code targets} may list multiple agents; {@code target} is a legacy
   * single-value fallback.
   */
  public record LlmRoutingDecision(String target, List<String> targets, String rationale) {}

  enum RouteTarget {
    ORCHESTRATE,
    COMMIT,
    STYLE,
    JOKE,
    GREETING
  }
}
