package com.dongho.springaiollama.utils;

import com.dongho.springaiollama.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileUtils {

    private final FileProcessingService fileProcessingService;
    private static final int MAX_FILE_SIZE_MB = 5;

    public void validateFiles(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            if (!fileProcessingService.isFileSizeValid(file, MAX_FILE_SIZE_MB)) {
                throw new IllegalArgumentException("File size exceeds the maximum limit of " + MAX_FILE_SIZE_MB + " MB");
            }
        }
    }
}
