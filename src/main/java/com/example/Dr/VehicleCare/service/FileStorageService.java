package com.example.Dr.VehicleCare.service;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.Dr.VehicleCare.security.FileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String original = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx >= 0) ext = original.substring(idx);
            String filename = UUID.randomUUID().toString() + ext;

            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }

    public FileResponse loadFileAsResource(String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();
            if (!Files.exists(filePath)) throw new RuntimeException("File not found " + filename);
            return new FileResponse(filePath.toAbsolutePath().toString(), Files.probeContentType(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + e.getMessage(), e);
        }
    }
}

