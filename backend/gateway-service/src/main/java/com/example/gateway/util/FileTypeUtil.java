package com.example.gateway.util;

import org.springframework.web.multipart.MultipartFile;

public class FileTypeUtil {

    public static String detectTypeByName(MultipartFile file) {
        if (file.getOriginalFilename() == null) return "unknown";

        String name = file.getOriginalFilename().toLowerCase();

        if (name.endsWith(".csv")) return "csv";
        if (name.endsWith(".txt")) return "txt";
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) return "xlsx";

        return "unknown";
    }
}
