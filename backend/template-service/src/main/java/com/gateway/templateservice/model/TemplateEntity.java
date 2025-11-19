package com.gateway.templateservice.model;

import java.util.List;
import java.util.Map;

public class TemplateEntity {
    private String id;
    private String category;
    private String originalFileName;
    private String fileType;
    private String storagePath;
    private Object extractedJson; // parsed content (List<Map<String,String>>)
    private Map<String, Object> metadataJson; // headers & rules

    public TemplateEntity() {
    }

    // getters & setters (omitted for brevity - add all)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Object getExtractedJson() {
        return extractedJson;
    }

    public void setExtractedJson(Object extractedJson) {
        this.extractedJson = extractedJson;
    }

    public Map<String, Object> getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(Map<String, Object> metadataJson) {
        this.metadataJson = metadataJson;
    }
}
