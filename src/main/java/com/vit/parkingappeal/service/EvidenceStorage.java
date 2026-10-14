package com.vit.parkingappeal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Stores evidence files under ${app.data-dir}/uploads. */
@Component
public class EvidenceStorage {

    private final Path uploadDir;

    public EvidenceStorage(@Value("${app.data-dir:./data}") String dataDir) {
        this.uploadDir = Path.of(dataDir).toAbsolutePath().resolve("uploads");
    }

    public String store(String appealRef, MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "evidence" : file.getOriginalFilename();
        String safeName = appealRef + "-" + original.replaceAll("[^A-Za-z0-9._-]", "_");
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(uploadDir);
            Files.copy(in, uploadDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
            return safeName;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store evidence file", e);
        }
    }

    public Path resolve(String storedName) {
        Path p = uploadDir.resolve(storedName).normalize();
        if (!p.startsWith(uploadDir)) {
            throw new NotFoundException("Evidence not found");
        }
        return p;
    }
}
