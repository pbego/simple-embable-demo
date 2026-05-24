package com.example.simpledemo.memory;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConversationMemoryProperties.class)
public class ConversationMemoryConfig {}
