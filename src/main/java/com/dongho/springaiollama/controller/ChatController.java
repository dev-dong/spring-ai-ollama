package com.dongho.springaiollama.controller;

import com.dongho.springaiollama.dto.ChatRequest;
import com.dongho.springaiollama.service.FileProcessingService;
import com.dongho.springaiollama.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient.Builder chatClient;
    private final FileProcessingService fileProcessingService;
    private final FileUtils fileUtils;

    @PostMapping()
    public String chat(@RequestBody ChatRequest request) {
        return chatClient.build()
                .prompt()
                .user(request.getMessage())
                .call()
                .content();
    }

    @PostMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        UserMessage userMessage = new UserMessage(request.getMessage());
        return streamChatMessages(userMessage);
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
            fileUtils.validateFiles(files);
        }

        UserMessage userMessage = fileProcessingService.createUserMessageWithFiles(message, files);
        return streamChatMessages(userMessage);
    }

    private Flux<String> streamChatMessages(UserMessage userMessage) {
        return chatClient.build()
                .prompt()
                .messages(userMessage)
                .stream()
                .content()
                .transform(chunk -> splitIntoSentences(chunk));
    }

    private Flux<String> splitIntoSentences(Flux<String> chunks) {
        return Flux.defer(() -> {
            log.info("Flux.defer 설정 - StringBuilder 최초 생성!");
            AtomicReference<StringBuilder> bufferRef = new AtomicReference<>(new StringBuilder());
            return chunks
                    .concatMap(chunk -> {
                        StringBuilder buffer = bufferRef.get();
                        buffer.append(chunk);

                        List<String> sentences = new ArrayList<>();
                        int cut;
                        while ((cut = findSentenceBoundary(buffer)) >= 0) {
                            String sentence = buffer.substring(0, cut + 1);
                            buffer.delete(0, cut + 1);
                            log.info("sentence extracted: {}", sentence);

                            if (!sentence.isBlank()) {
                                sentences.add(sentence);
                            }
                        }

                        return Flux.fromIterable(sentences);
                    })
                    .concatWith(Flux.defer(() -> {
                        log.info("내 응답 완료!");
                        String rest = bufferRef.get().toString();
                        return rest.isBlank() ? Flux.empty() : Flux.just(rest);
                    }));
        });
    }

    private int findSentenceBoundary(StringBuilder buffer) {
        return buffer.indexOf("\n");
    }
}
