package com.example.simpledemo.decision;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(DecisionModelProperties.class)
public class DecisionModelConfiguration {

  @Bean
  DecisionModelClient decisionModelClient(
      RestClient.Builder restClientBuilder, DecisionModelProperties properties) {
    return new DecisionModelClient(restClientBuilder.baseUrl(properties.url()).build());
  }
}
