package com.example.simpledemo.chat;

import com.embabel.chat.Conversation;
import com.embabel.chat.Message;
import com.embabel.chat.SystemMessage;
import com.example.simpledemo.memory.ConversationMemoryState;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class ChatPromptBuilder {

  private static final String SUMMARY_HEADER =
      """
      Prior conversation summary (older turns compressed):
      """
          .strip();

  List<Message> build(
      String systemPrompt, Conversation conversation, ConversationMemoryState memory, int recentCount) {
    var messages = new ArrayList<Message>();
    messages.add(new SystemMessage(systemPrompt));
    if (memory.hasSummary()) {
      messages.add(new SystemMessage(SUMMARY_HEADER + "\n" + memory.sessionSummary()));
    }
    messages.addAll(conversation.last(recentCount).getMessages());
    return messages;
  }
}
