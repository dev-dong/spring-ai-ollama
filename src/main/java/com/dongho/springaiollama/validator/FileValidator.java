package com.dongho.springaiollama.validator;

import com.dongho.springaiollama.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private final FileProperties properties;

    public void validate(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            validateNotEmpty(file);
            validateFileSize(file);
        }
    }

    public boolean isSupportedExtension(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return properties.getAllowedExtensions().stream().anyMatch(ext -> ext.endsWith(lower));
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > (long) properties.getMaxFileSizeMb() * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size");
        }
    }
}
