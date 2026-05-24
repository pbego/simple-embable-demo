package com.example.simpledemo.config;

import com.embabel.agent.api.validation.guardrails.GuardRailConfiguration;
import com.example.simpledemo.security.CommitSafetyGuardRail;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoGuardRailConfiguration {

  @Bean
  GuardRailConfiguration guardRailConfiguration(CommitSafetyGuardRail commitSafetyGuardRail) {
    return new GuardRailConfiguration(List.of(commitSafetyGuardRail));
  }
}
