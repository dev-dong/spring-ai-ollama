package com.dongho.springaiollama.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.file")
@Data
public class FileProperties {
    private int maxFileSizeMb;
    private List<String> allowedExtensions;
}
