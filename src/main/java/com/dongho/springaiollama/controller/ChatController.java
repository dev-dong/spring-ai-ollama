package com.dongho.springaiollama.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatClient.Builder chatClient;

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.build()
                .prompt()
                .user(message)
                .call()
                .content();
    }
}
