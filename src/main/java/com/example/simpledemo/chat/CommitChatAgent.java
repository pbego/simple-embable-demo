package com.example.simpledemo.chat;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.PlannerType;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Conversation;
import com.embabel.chat.UserMessage;
import com.embabel.chat.agent.ConversationContinues;
import com.example.simpledemo.memory.ConversationMemoryAccessor;
import com.example.simpledemo.memory.ConversationMemoryProperties;
import com.example.simpledemo.memory.ConversationMemoryState;

/**
 * Minimal chat agent for the history/memory demo. Uses an inline system prompt (no Jinja).
 * Conversation persistence is handled by {@link com.example.simpledemo.memory.PersistingConversation}.
 */
@Agent(
    name = "Commit chat",
    description = "Persistent chat with conversation history",
    planner = PlannerType.UTILITY)
public class CommitChatAgent {

  private static final String SYSTEM_PROMPT =
      """
      You are a helpful assistant for developers working in a git repository.
      You help discuss changes and Conventional Commits-style messages.
      Keep replies concise. Use prior messages in the conversation for context.
      """
          .strip();

  private final ConversationMemoryProperties memoryProperties;
  private final SessionSummaryService sessionSummaryService;
  private final ChatPromptBuilder chatPromptBuilder;

  public CommitChatAgent(
      ConversationMemoryProperties memoryProperties,
      SessionSummaryService sessionSummaryService,
      ChatPromptBuilder chatPromptBuilder) {
    this.memoryProperties = memoryProperties;
    this.sessionSummaryService = sessionSummaryService;
    this.chatPromptBuilder = chatPromptBuilder;
  }

  @Action(canRerun = true, trigger = UserMessage.class, description = "Reply using conversation history")
  public ConversationContinues respond(Conversation conversation, ActionContext context) {
    sessionSummaryService.refreshSummaryIfNeeded(conversation, context);

    var memory = memoryState(conversation);
    var messages =
        chatPromptBuilder.build(
            SYSTEM_PROMPT, conversation, memory, memoryProperties.memoryMaxMessages());

    var reply = context.ai().withDefaultLlm().createObject(messages, String.class);
    var assistantMessage = new AssistantMessage(reply);
    conversation.addMessage(assistantMessage);
    context.sendMessage(assistantMessage);
    return ConversationContinues.with(assistantMessage);
  }

  private static ConversationMemoryState memoryState(Conversation conversation) {
    if (conversation instanceof ConversationMemoryAccessor accessor) {
      return accessor.memoryState();
    }
    return ConversationMemoryState.empty();
  }
}
