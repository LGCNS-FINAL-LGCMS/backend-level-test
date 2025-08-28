package com.lgcms.leveltest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final BedrockProxyChatModel bedrockProxyChatModel;

    @Bean
    public ChatClient gradingChatClient() {
        return ChatClient.builder(bedrockProxyChatModel)
                .defaultOptions(ChatOptions.builder()
                        .model("anthropic.claude-3-haiku-20240307-v1:0")
                        .temperature(0.1)
                        .maxTokens(2000)
                        .build())
                .build();
    }

    @Bean
    public ChatClient feedbackChatClient() {
        return ChatClient.builder(bedrockProxyChatModel)
                .defaultOptions(ChatOptions.builder()
                        .model("anthropic.claude-3-haiku-20240307-v1:0")
                        .temperature(0.3)
                        .maxTokens(1500)
                        .build())
                .build();
    }

    @Bean
    public ChatClient conceptAnalysisChatClient() {
        return ChatClient.builder(bedrockProxyChatModel)
                .defaultOptions(ChatOptions.builder()
                        .model("anthropic.claude-3-haiku-20240307-v1:0")
                        .temperature(0.2)
                        .maxTokens(800)
                        .build())
                .build();
    }
}
