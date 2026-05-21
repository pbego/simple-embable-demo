package com.example.simpledemo.memory;

import com.embabel.chat.Message;
import java.util.List;
import java.util.Optional;

/** Decides which messages should be folded into the rolling session summary. */
public final class ConversationSummarizationPlanner {

  private ConversationSummarizationPlanner() {}

  /**
   * @param summarizedThroughIndex last message index already covered by {@code sessionSummary},
   *     or {@link ConversationMemoryState#NONE_SUMMARIZED}
   * @param keepRecent how many trailing messages stay verbatim in the LLM prompt
   */
  public static Optional<SummarizationSlice> messagesToSummarize(
      List<Message> messages, int summarizedThroughIndex, int keepRecent) {
    if (messages.isEmpty() || messages.size() <= keepRecent) {
      return Optional.empty();
    }
    var recentStart = messages.size() - keepRecent;
    var nextIndex = summarizedThroughIndex + 1;
    if (nextIndex >= recentStart) {
      return Optional.empty();
    }
    return Optional.of(
        new SummarizationSlice(messages.subList(nextIndex, recentStart), recentStart - 1));
  }

  public record SummarizationSlice(List<Message> messages, int newSummarizedThroughIndex) {}
}
