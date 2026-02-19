package com.dongho.springaiollama.controller;

import com.dongho.springaiollama.dto.ChatRequest;
import com.dongho.springaiollama.service.ChatStreamingService;
import com.dongho.springaiollama.service.FileProcessingService;
import com.dongho.springaiollama.validator.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final FileProcessingService fileProcessingService;
    private final ChatStreamingService chatStreamingService;
    private final FileValidator validator;

    @PostMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        UserMessage userMessage = new UserMessage(request.getMessage());
        return chatStreamingService.stream(userMessage);
    }

    /**
     * curl -N -X POST http://localhost:8080/api/chat/stream/with-files \
     * -F "message=이 Java 파일들을 분석해서 버그를 찾아줘" \
     * -F "files=@UserService.java" \
     * -F "files=@UserRepository.java"
     */
    @PostMapping(value = "/stream/with-files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithFiles(
            @RequestParam("message") String message,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        if (files != null) {
            validator.validate(files);
        }

        UserMessage userMessage = fileProcessingService.createUserMessageWithFiles(message, files);
        return chatStreamingService.stream(userMessage);
    }
}
