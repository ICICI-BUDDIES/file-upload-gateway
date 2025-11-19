package com.gateway.templateservice.storage;

import com.gateway.templateservice.model.TemplateEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Component
public class TemplateStorage {

    private final Path baseDir = Paths.get("storage/templates");
    private final Map<String, TemplateEntity> repo = new LinkedHashMap<>();

    public TemplateStorage() throws IOException {
        if (!Files.exists(baseDir))
            Files.createDirectories(baseDir);
    }

    public TemplateEntity save(TemplateEntity t, byte[] content) throws IOException {
        Path categoryDir = baseDir.resolve(t.getCategory());
        if (!Files.exists(categoryDir))
            Files.createDirectories(categoryDir);

        String filename = t.getId() + "_" + t.getOriginalFileName();
        Path filePath = categoryDir.resolve(filename);
        Files.write(filePath, content, StandardOpenOption.CREATE);

        t.setStoragePath(filePath.toAbsolutePath().toString());
        repo.put(t.getId(), t);
        return t;
    }

    public TemplateEntity findById(String id) {
        return repo.get(id);
    }

    public TemplateEntity findByCategory(String category) {
        for (TemplateEntity t : repo.values()) {
            if (t.getCategory().equalsIgnoreCase(category))
                return t;
        }
        return null;
    }

    public Collection<TemplateEntity> findAll() {
        return repo.values();
    }

    public void delete(String id) {
        TemplateEntity t = repo.remove(id);
        if (t != null && t.getStoragePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(t.getStoragePath()));
            } catch (Exception e) {
                /* ignore */ }
        }
    }

    public byte[] getFileBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }
}
