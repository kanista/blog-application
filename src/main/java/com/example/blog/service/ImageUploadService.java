package com.example.blog.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageUploadService {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public String uploadImage(MultipartFile file) throws IOException {
        System.out.println("Upload directory: " + UPLOAD_DIR);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }

        // Ensure the directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            System.out.println("Directory does not exist, creating: " + UPLOAD_DIR);
            directory.mkdirs();
        }

        // Generate a unique file name
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        try {
            // Save the file locally
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File successfully saved: " + filePath.toString());
        } catch (IOException e) {
            System.err.println("Error while saving file: " + e.getMessage());
            throw e; // Re-throw the exception after logging
        }

        // Return the file URL for access
        return "/uploads/" + fileName;
    }
}
