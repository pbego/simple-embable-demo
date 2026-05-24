package com.example.simpledemo.api;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.example.simpledemo.agent.CommitMessage;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("api")
@RequestMapping("/api/demo")
public class CommitProcessController {

  private final AgentPlatform agentPlatform;

  public CommitProcessController(AgentPlatform agentPlatform) {
    this.agentPlatform = agentPlatform;
  }

  @PostMapping("/commit")
  public Map<String, String> startCommit(@RequestBody CommitRequestBody body) throws Exception {
    var hint = body != null && body.hint() != null ? body.hint() : "";
    var process =
        AgentInvocation.create(agentPlatform, CommitMessage.class)
            .runAsync(new UserInput(hint))
            .get(2, TimeUnit.MINUTES);

    return Map.of(
        "processId",
        process.getId(),
        "statusUrl",
        "/api/v1/process/" + process.getId(),
        "eventsUrl",
        "/events/process/" + process.getId());
  }

  public record CommitRequestBody(String hint) {}
}
