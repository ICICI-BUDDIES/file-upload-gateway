package com.example.gateway.service;

import com.example.gateway.util.FileTypeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class FileValidationService {

    private final Set<String> allowedApps;

    // Size in MB (configured in application.properties)
    @Value("${gateway.max-file-mb}")
    private int maxFileMb;

    public FileValidationService(@Value("${gateway.allowed.applications}") String allowedAppsCsv) {
        this.allowedApps = new HashSet<>(Arrays.asList(allowedAppsCsv.split(",")));
    }

    public void validate(MultipartFile file, String application, String declaredFormat) {

        // 1. File must exist
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded. Please select a file and try again.");
        }

        // 2. Valid application (skip validation if wildcard is set)
        if (application == null) {
            throw new IllegalArgumentException("Application identifier is missing. Please contact support.");
        }
        
        if (!allowedApps.contains("*") && !allowedApps.contains(application)) {
            throw new IllegalArgumentException("Application '" + application + "' is not authorized to upload files. Please contact your administrator.");
        }

        // 3. File size in MB
        long fileSizeBytes = file.getSize();
        long maxBytes = maxFileMb * 1024L * 1024L;
        double fileSizeMB = fileSizeBytes / (1024.0 * 1024.0);

        if (fileSizeBytes > maxBytes) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds the allowed limit of %d MB. Your file is %.1f MB. Please reduce the file size and try again.", 
                    maxFileMb, fileSizeMB)
            );
        }

        // 4. Format validation (if declared)
        String detected = FileTypeUtil.detectTypeByName(file);

        if (declaredFormat != null && !declaredFormat.trim().isEmpty()) {
            if (!declaredFormat.equalsIgnoreCase(detected)) {
                throw new IllegalArgumentException(
                        String.format("File format mismatch. Expected '%s' file but received '%s'. Please upload the correct file format.", 
                        declaredFormat.toUpperCase(), detected.toUpperCase())
                );
            }
        }
    }
}
