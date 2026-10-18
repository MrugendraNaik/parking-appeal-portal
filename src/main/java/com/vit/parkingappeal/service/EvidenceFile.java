package com.vit.parkingappeal.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * What the validator needs to know about an uploaded evidence file:
 * its name, size and first few bytes (to check the real file type, not just the extension).
 */
public record EvidenceFile(String originalName, long size, byte[] header) {

    public static EvidenceFile from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        byte[] header = new byte[8];
        int read = 0;
        try (InputStream in = file.getInputStream()) {
            int n;
            while (read < header.length && (n = in.read(header, read, header.length - read)) > 0) {
                read += n;
            }
        } catch (IOException e) {
            read = 0;
        }
        byte[] exact = new byte[read];
        System.arraycopy(header, 0, exact, 0, read);
        String name = file.getOriginalFilename() == null ? "evidence" : file.getOriginalFilename();
        return new EvidenceFile(name, file.getSize(), exact);
    }

    public String extension() {
        int dot = originalName.lastIndexOf('.');
        return dot < 0 ? "" : originalName.substring(dot + 1).toLowerCase();
    }
}
