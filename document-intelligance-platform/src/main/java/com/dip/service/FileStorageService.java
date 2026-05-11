package com.dip.service;

import com.dip.config.DipStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final DipStorageProperties properties;

    public FileStorageService(DipStorageProperties properties) {
        this.properties = properties;
    }

    public Path savePdf(UUID id, MultipartFile file) throws IOException {
        Path root = Path.of(properties.root()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        Path target = root.resolve(id + ".pdf");
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }
}
