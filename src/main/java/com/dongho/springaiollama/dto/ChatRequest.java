package com.dongho.springaiollama.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ChatRequest {
    private String message;
    private List<MultipartFile> files;

    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    public List<MultipartFile> getFilesByExtension(String extension) {
        if (!hasFiles()) {
            return List.of();
        }

        return files.stream()
                .filter(file -> {
                    String filename = file.getOriginalFilename();
                    return filename != null && filename.toLowerCase().endsWith(extension.toLowerCase());
                })
                .toList();
    }
}
