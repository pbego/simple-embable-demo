package com.example.simpledemo.domain;

import com.example.simpledemo.agent.CommitMessage;

/**
 * Typed LLM output for a proposed commit message.
 */
public record CommitProposal(String subject, String body) {

  public static CommitProposal from(CommitMessage message) {
    if (message == null) {
      return new CommitProposal("", "");
    }
    return new CommitProposal(
        message.subject() == null ? "" : message.subject(),
        message.body() == null ? "" : message.body());
  }

  public CommitMessage toCommitMessage() {
    return new CommitMessage(subject, body);
  }

  public String formatted() {
    return toCommitMessage().formatted();
  }
}
