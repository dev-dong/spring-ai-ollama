package com.dongho.springaiollama.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class FileProcessingService {

    private static final List<String> TEXT_EXTENSIONS = List.of(".py", ".js", ".java");

    public UserMessage createUserMessageWithFiles(String messageText, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new UserMessage(messageText);
        }

        try {
            String filesContent = buildFilesContent(files);
            String userMessageContent = String.format("""
                    %s
                    
                    다음은 참고할 파일들입니다:
                    %s
                    
                    위 파일들을 참고하여 질문에 답변해주세요.
                    """, messageText, filesContent);
            return new UserMessage(userMessageContent);
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생", e);
            return new UserMessage(messageText + "\n\n[참고: 파일 처리 중 오류가 발생했습니다.]");
        }
    }

    private String buildFilesContent(List<MultipartFile> files) throws IOException {
        StringBuilder builder = new StringBuilder();

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            boolean isTextFile = TEXT_EXTENSIONS.stream().anyMatch(ext -> filename != null && filename.toLowerCase().endsWith(ext));
            if (!isTextFile) {
                continue;
            }

            String fileContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            builder.append("\n\n=== ").append(filename).append("===\n");
            builder.append(fileContent);
        }

        return builder.toString();
    }

    public boolean isFileSizeValid(MultipartFile file, int maxSizeInMB) {
        long maxBytes = maxSizeInMB * 1024L * 1024L;
        return file.getSize() <= maxBytes;
    }
}
