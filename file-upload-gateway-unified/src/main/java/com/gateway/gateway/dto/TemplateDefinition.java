package com.gateway.gateway.dto;

import java.util.List;
import java.util.Map;

public class TemplateDefinition {
    private String templateName;
    private String fileType;
    private List<String> headers;
    private Map<String, Object> structureRules;

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }

    public Map<String, Object> getStructureRules() { return structureRules; }
    public void setStructureRules(Map<String, Object> structureRules) { this.structureRules = structureRules; }
}
