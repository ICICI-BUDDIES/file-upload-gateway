package com.gateway.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.gateway.dto.TemplateDefinition;
import com.gateway.template.model.TemplateEntity;
import com.gateway.template.storage.TemplateStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TemplateLookupService {

    @Autowired
    private TemplateStorage templateStorage;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** --------------------------------------------------------------
     * 1. Fetch ALL template entities from database
     * -------------------------------------------------------------- */
    public Object fetchAllTemplatesRaw() {
        return templateStorage.findAll();
    }

    /** --------------------------------------------------------------
     * 2. Convert raw template JSON → List<String> categories
     * -------------------------------------------------------------- */
    public List<String> extractCategories(Object raw) {
        if (raw == null) return Collections.emptyList();

        List<String> categories = new ArrayList<>();

        if (raw instanceof List) {
            List<?> list = (List<?>) raw;

            for (Object o : list) {
                if (o instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) o;

                    Object category = m.get("category");
                    if (category != null) {
                        categories.add(String.valueOf(category));
                    }
                }
            }
        }
        return categories;
    }

    /** --------------------------------------------------------------
     * 3. Fetch metadata for app-specific template
     * -------------------------------------------------------------- */
    public TemplateDefinition fetchAppTemplate(String appNameHash, String category) {
        System.out.println("➡ Fetching app template metadata for: " + appNameHash + "/" + category);
        
        try {
            TemplateEntity template = templateStorage.findByAppHashAndCategory(appNameHash, category);
            if (template == null) {
                throw new RuntimeException("Template not found");
            }
            
            TemplateDefinition def = new TemplateDefinition();
            def.setTemplateName(template.getCategory());
            def.setFileType(template.getFileType());
            
            if (template.getMetadataJson() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = objectMapper.readValue(template.getMetadataJson(), Map.class);
                
                // Set headers
                if (metadata.containsKey("headers")) {
                    @SuppressWarnings("unchecked")
                    List<String> headers = (List<String>) metadata.get("headers");
                    def.setHeaders(headers);
                }
                
                // Set structure rules (includes both rules and fieldConfig)
                Map<String, Object> allRules = new java.util.HashMap<>();
                if (metadata.containsKey("rules")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> basicRules = (Map<String, Object>) metadata.get("rules");
                    allRules.putAll(basicRules);
                }
                if (metadata.containsKey("fieldConfig")) {
                    allRules.put("fieldConfig", metadata.get("fieldConfig"));
                }
                def.setStructureRules(allRules);
            }
            
            return def;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch app template metadata: " + e.getMessage());
        }
    }
    
    /** --------------------------------------------------------------
     * 4. Fetch categories for specific app
     * -------------------------------------------------------------- */
    public List<String> fetchAppCategories(String appNameHash) {
        System.out.println("➡ Fetching app categories for: " + appNameHash);
        
        try {
            List<TemplateEntity> templates = templateStorage.findByAppHash(appNameHash);
            return templates.stream()
                    .map(TemplateEntity::getCategory)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch app categories: " + e.getMessage());
        }
    }
    
    /** --------------------------------------------------------------
     * 5. Legacy method - Fetch metadata for a specific template category
     * -------------------------------------------------------------- */
    @Deprecated
    public TemplateDefinition fetchTemplate(String category) {
        System.out.println("➡ Fetching template metadata for category: " + category);
        
        try {
            TemplateEntity template = templateStorage.findByCategory(category);
            if (template == null) {
                throw new RuntimeException("Template not found");
            }
            
            TemplateDefinition def = new TemplateDefinition();
            def.setTemplateName(template.getCategory());
            def.setFileType(template.getFileType());
            
            if (template.getMetadataJson() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = objectMapper.readValue(template.getMetadataJson(), Map.class);
                if (metadata.containsKey("structureRules")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rules = (Map<String, Object>) metadata.get("structureRules");
                    def.setStructureRules(rules);
                }
            }
            
            return def;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch template metadata: " + e.getMessage());
        }
    }
}
