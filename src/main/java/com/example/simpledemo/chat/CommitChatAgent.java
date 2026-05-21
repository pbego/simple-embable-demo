package com.example.simpledemo.chat;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.PlannerType;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Conversation;
import com.embabel.chat.Message;
import com.embabel.chat.SystemMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.agent.ConversationContinues;
import com.example.simpledemo.memory.ConversationMemoryProperties;
import java.util.ArrayList;
import java.util.List;

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

  public CommitChatAgent(ConversationMemoryProperties memoryProperties) {
    this.memoryProperties = memoryProperties;
  }

  @Action(canRerun = true, trigger = UserMessage.class, description = "Reply using conversation history")
  public ConversationContinues respond(Conversation conversation, ActionContext context) {
    var windowed = conversation.last(memoryProperties.memoryMaxMessages());
    var messages = new ArrayList<Message>();
    messages.add(new SystemMessage(SYSTEM_PROMPT));
    messages.addAll(windowed.getMessages());

    var reply = context.ai().withDefaultLlm().createObject(messages, String.class);
    var assistantMessage = new AssistantMessage(reply);
    conversation.addMessage(assistantMessage);
    context.sendMessage(assistantMessage);
    return ConversationContinues.with(assistantMessage);
  }
}
