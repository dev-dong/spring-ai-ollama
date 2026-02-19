package com.dongho.springaiollama.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatStreamingService {

    private final ChatClient client;

    public Flux<String> stream(UserMessage message) {
        return client
                .prompt()
                .messages(message)
                .stream()
                .content()
                .transform(chunk -> splitByNewLine(chunk));
    }

    private Flux<String> splitByNewLine(Flux<String> chunks) {
        return Flux.defer(() -> {
            log.info("Flux.defer 설정 - StringBuilder 최초 생성!");
            AtomicReference<StringBuilder> bufferRef = new AtomicReference<>(new StringBuilder());
            return chunks
                    .concatMap(chunk -> {
                        StringBuilder buffer = bufferRef.get();
                        buffer.append(chunk);

                        List<String> lines = new ArrayList<>();
                        int cut;
                        while ((cut = buffer.indexOf("\n")) >= 0) {
                            String line = buffer.substring(0, cut + 1);
                            buffer.delete(0, cut + 1);
                            log.info("sentence extracted: {}", line);

                            if (!line.isBlank()) lines.add(line);
                        }

                        return Flux.fromIterable(lines);
                    })
                    .concatWith(Flux.defer(() -> {
                        log.info("내 응답 완료!");
                        String rest = bufferRef.get().toString();
                        return rest.isBlank() ? Flux.empty() : Flux.just(rest);
                    }));
        });
    }
}
