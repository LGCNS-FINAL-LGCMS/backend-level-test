package com.lgcms.leveltest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatClientConfig {

    private final ChatClient.Builder chatClientBuilder;

    public ChatClient getChatClient(double temperature, int maxTokens) {
        return chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .maxTokens(maxTokens)
                        .temperature(temperature)
                        .build())
                .build();
    }
}
