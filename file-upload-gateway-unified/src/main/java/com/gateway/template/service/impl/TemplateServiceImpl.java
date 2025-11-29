package com.gateway.template.service.impl;

import com.gateway.template.dto.RegistrationRequest;
import com.gateway.template.dto.RegistrationResponse;
import com.gateway.template.dto.TemplateMetadataResponse;
import com.gateway.template.dto.TemplateResponse;
import com.gateway.template.util.HashUtil;

import com.gateway.template.model.TemplateEntity;
import com.gateway.template.parser.CSVParser;
import com.gateway.template.parser.PipeParser;
import com.gateway.template.parser.TxtParser;
import com.gateway.template.parser.ExcelParser;
import com.gateway.template.service.TemplateService;
import com.gateway.template.storage.TemplateStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateStorage storage;
    
    @Autowired
    private HashUtil hashUtil;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public RegistrationResponse registerApp(RegistrationRequest request, MultipartFile templateFile) {
        return registerApp(request, templateFile, null);
    }
    
    @Override
    public RegistrationResponse registerApp(RegistrationRequest request, MultipartFile templateFile, String headerConfig) {
        try {
            String appNameHash = hashUtil.hashAppName(request.getAppName());
            
            // Check if app-category combination already exists
            if (storage.existsByAppHashAndCategory(appNameHash, request.getCategory())) {
                // Update existing template
                TemplateEntity existingTemplate = storage.findByAppHashAndCategory(appNameHash, request.getCategory());
                updateTemplate(existingTemplate, templateFile, request, headerConfig);
                return new RegistrationResponse(true, "Template updated successfully", appNameHash);
            } else {
                // Create new template
                TemplateEntity newTemplate = createTemplate(templateFile, request, appNameHash, headerConfig);
                storage.save(newTemplate, templateFile.getBytes());
                return new RegistrationResponse(true, "App registered successfully", appNameHash);
            }
        } catch (Exception e) {
            return new RegistrationResponse(false, "Registration failed: " + e.getMessage(), null);
        }
    }
    
    @Override
    public List<String> getCategoriesByAppHash(String appNameHash) {
        List<TemplateEntity> templates = storage.findByAppHash(appNameHash);
        return templates.stream()
                .map(TemplateEntity::getCategory)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public TemplateMetadataResponse getMetadataByAppAndCategory(String appNameHash, String category) {
        TemplateEntity t = storage.findByAppHashAndCategory(appNameHash, category);
        if (t == null)
            throw new RuntimeException("Template not found for app and category");
        try {
            TemplateMetadataResponse resp = new TemplateMetadataResponse();
            resp.setTemplateName(t.getOriginalFileName());
            resp.setFileType(t.getFileType());
            resp.setEndpoint(t.getEndpoint());
            
            Map<String, Object> metaMap = mapper.readValue(t.getMetadataJson(), Map.class);
            List<String> headers = (List<String>) metaMap.get("headers");
            resp.setHeaders(headers);
            
            // Include all metadata as structure rules (rules + fieldConfig)
            Map<String, Object> allRules = new HashMap<>();
            Map<String, Object> basicRules = (Map<String, Object>) metaMap.get("rules");
            if (basicRules != null) {
                allRules.putAll(basicRules);
            }
            // Add fieldConfig if it exists
            if (metaMap.containsKey("fieldConfig")) {
                allRules.put("fieldConfig", metaMap.get("fieldConfig"));
            }
            resp.setStructureRules(allRules);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Object getJsonByAppAndCategory(String appNameHash, String category) {
        TemplateEntity t = storage.findByAppHashAndCategory(appNameHash, category);
        if (t == null)
            throw new RuntimeException("Template not found for app and category");
        try {
            return mapper.readValue(t.getExtractedJson(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public byte[] downloadByAppAndCategory(String appNameHash, String category) {
        TemplateEntity t = storage.findByAppHashAndCategory(appNameHash, category);
        if (t == null)
            return null;
        return storage.getFileBytes(t);
    }
    
    @Override
    public TemplateEntity getByAppAndCategory(String appNameHash, String category) {
        return storage.findByAppHashAndCategory(appNameHash, category);
    }

    @Override
    @Deprecated
    public TemplateResponse uploadTemplate(MultipartFile file, String category, String format, String templateName) {
        try {
            String ext = format == null || format.trim().isEmpty() ? getExt(file.getOriginalFilename())
                    : format.toLowerCase();
            TemplateEntity entity = new TemplateEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setCategory(category);
            entity.setOriginalFileName(templateName == null ? file.getOriginalFilename() : templateName);
            entity.setFileType(ext);

            byte[] bytes = file.getBytes();
            Object parsed = parseBytes(bytes, ext);
            entity.setExtractedJson(mapper.writeValueAsString(parsed));

            Map<String, Object> meta = buildMetadata(parsed);
            entity.setMetadataJson(mapper.writeValueAsString(meta));

            storage.save(entity, bytes);

            TemplateResponse resp = new TemplateResponse();
            resp.setId(entity.getId());
            resp.setCategory(entity.getCategory());
            resp.setFileName(entity.getOriginalFileName());
            resp.setFileType(entity.getFileType());
            resp.setMessage("uploaded");
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<TemplateEntity> listAll() {
        return storage.findAll();
    }

    @Override
    public TemplateEntity getById(String id) {
        return storage.findById(id);
    }

    @Override
    public void delete(String id) {
        storage.delete(id);
    }

    @Override
    @Deprecated
    public TemplateMetadataResponse getMetadataByCategory(String category) {
        TemplateEntity t = storage.findByCategory(category);
        if (t == null)
            throw new RuntimeException("not found");
        try {
            TemplateMetadataResponse resp = new TemplateMetadataResponse();
            resp.setTemplateName(t.getOriginalFileName());
            resp.setFileType(t.getFileType());
            
            Map<String, Object> metaMap = mapper.readValue(t.getMetadataJson(), Map.class);
            List<String> headers = (List<String>) metaMap.get("headers");
            resp.setHeaders(headers);
            resp.setStructureRules((Map<String, Object>) metaMap.get("rules"));
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public Object getJsonByCategory(String category) {
        TemplateEntity t = storage.findByCategory(category);
        if (t == null)
            throw new RuntimeException("not found");
        try {
            return mapper.readValue(t.getExtractedJson(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public byte[] downloadByCategoryAndFormat(String category, String format) {
        TemplateEntity t = storage.findByCategory(category);
        if (t == null)
            return null;
        return storage.getFileBytes(t);
    }

    @Override
    public boolean pushMetadata(String id, String url) {
        TemplateEntity t = storage.findById(id);
        if (t == null)
            return false;
        try {
            RestTemplate rt = new RestTemplate();
            Object metaObj = mapper.readValue(t.getMetadataJson(), Object.class);
            rt.postForObject(url, metaObj, String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- helpers ----------
    private Object parseBytes(byte[] bytes, String ext) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        System.out.println("🔍 Parsing file with extension: " + ext);
        
        switch (ext.toLowerCase()) {
            case "csv":
                System.out.println("📈 Using CSV parser");
                return CSVParser.parse(bis);
            case "txt":
                System.out.println("📄 Using TXT parser");
                return TxtParser.parse(bis);
            case "psv":
            case "pipe":
                System.out.println("📄 Using Pipe parser");
                return PipeParser.parse(bis);
            case "xls":
            case "xlsx":
                System.out.println("📊 Using Excel parser");
                return ExcelParser.parse(bis);
            default:
                System.err.println("❌ Unsupported file extension: " + ext);
                throw new RuntimeException("Unsupported file type: " + ext);
        }
    }

    private Map<String, Object> buildMetadata(Object parsed) {
        return buildMetadata(parsed, null);
    }
    
    private Map<String, Object> buildMetadata(Object parsed, String headerConfig) {
        Map<String, Object> meta = new LinkedHashMap<>();
        List<Map<String, String>> rows = (List<Map<String, String>>) parsed;
        List<String> headers = new ArrayList<>();
        if (!rows.isEmpty())
            headers.addAll(rows.get(0).keySet());
        meta.put("headers", headers);

        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("minRows", 1);
        rules.put("maxRows", 10000);
        rules.put("strictColumnOrder", true);
        rules.put("allowExtraColumns", false);

        meta.put("rules", rules);
        
        // Add header configuration if provided
        if (headerConfig != null && !headerConfig.trim().isEmpty()) {
            try {
                Map<String, Object> config = mapper.readValue(headerConfig, Map.class);
                meta.put("fieldConfig", config);
            } catch (Exception e) {
                System.err.println("Failed to parse header config: " + e.getMessage());
            }
        }
        
        return meta;
    }

    private TemplateEntity createTemplate(MultipartFile file, RegistrationRequest request, String appNameHash) throws Exception {
        return createTemplate(file, request, appNameHash, null);
    }
    
    private TemplateEntity createTemplate(MultipartFile file, RegistrationRequest request, String appNameHash, String headerConfig) throws Exception {
        String ext = getExt(file.getOriginalFilename());
        System.out.println("📁 Creating template - File: " + file.getOriginalFilename() + ", Extension: " + ext);
        
        TemplateEntity entity = new TemplateEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setAppName(request.getAppName());
        entity.setAppNameHash(appNameHash);
        entity.setEndpoint(request.getEndpoint());
        entity.setCategory(request.getCategory());
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setFileType(ext);

        byte[] bytes = file.getBytes();
        System.out.println("📁 File size: " + bytes.length + " bytes");
        
        try {
            Object parsed = parseBytes(bytes, ext);
            System.out.println("✅ Parsing successful");
            
            entity.setExtractedJson(mapper.writeValueAsString(parsed));
            Map<String, Object> meta = buildMetadata(parsed, headerConfig);
            entity.setMetadataJson(mapper.writeValueAsString(meta));
            
            // Validate template against its own field configuration
            if (headerConfig != null && !headerConfig.trim().isEmpty()) {
                validateTemplateAgainstFieldConfig(parsed, headerConfig);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Template validation failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return entity;
    }
    
    private void updateTemplate(TemplateEntity existingTemplate, MultipartFile file, RegistrationRequest request) throws Exception {
        updateTemplate(existingTemplate, file, request, null);
    }
    
    private void updateTemplate(TemplateEntity existingTemplate, MultipartFile file, RegistrationRequest request, String headerConfig) throws Exception {
        String ext = getExt(file.getOriginalFilename());
        existingTemplate.setEndpoint(request.getEndpoint());
        existingTemplate.setOriginalFileName(file.getOriginalFilename());
        existingTemplate.setFileType(ext);

        byte[] bytes = file.getBytes();
        Object parsed = parseBytes(bytes, ext);
        existingTemplate.setExtractedJson(mapper.writeValueAsString(parsed));

        Map<String, Object> meta = buildMetadata(parsed, headerConfig);
        existingTemplate.setMetadataJson(mapper.writeValueAsString(meta));
        
        // Validate template against its own field configuration
        if (headerConfig != null && !headerConfig.trim().isEmpty()) {
            validateTemplateAgainstFieldConfig(parsed, headerConfig);
        }
        
        storage.save(existingTemplate, bytes);
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains("."))
            return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
    
    @Override
    public List<String> extractHeaders(MultipartFile file) {
        try {
            String ext = getExt(file.getOriginalFilename());
            System.out.println("🔍 Extracting headers from file: " + file.getOriginalFilename() + ", extension: " + ext);
            
            if (!ext.equals("csv") && !ext.equals("xls") && !ext.equals("xlsx") && !ext.equals("txt")) {
                throw new RuntimeException("Only CSV, XLS, XLSX, and TXT files are supported for header extraction");
            }
            
            byte[] bytes = file.getBytes();
            Object parsed = parseBytes(bytes, ext);
            
            List<Map<String, String>> rows = (List<Map<String, String>>) parsed;
            if (rows.isEmpty()) {
                System.out.println("⚠️ No rows found in parsed data");
                return new ArrayList<>();
            }
            
            List<String> headers = new ArrayList<>(rows.get(0).keySet());
            System.out.println("📋 Extracted headers: " + headers);
            return headers;
        } catch (Exception e) {
            System.err.println("❌ Failed to extract headers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to extract headers: " + e.getMessage());
        }
    }
    
    private void validateTemplateAgainstFieldConfig(Object parsed, String headerConfig) throws Exception {
        Map<String, Object> fieldConfig = mapper.readValue(headerConfig, Map.class);
        List<Map<String, String>> rows = (List<Map<String, String>>) parsed;
        
        if (rows.isEmpty()) {
            return; // No data to validate
        }
        
        List<String> validationErrors = new ArrayList<>();
        
        // Validate each row against field configuration
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, String> row = rows.get(rowIndex);
            
            for (Map.Entry<String, Object> configEntry : fieldConfig.entrySet()) {
                String fieldName = configEntry.getKey();
                Map<String, Object> fieldRules = (Map<String, Object>) configEntry.getValue();
                
                String fieldValue = row.get(fieldName);
                String error = validateTemplateField(fieldName, fieldValue, fieldRules, rowIndex + 1);
                
                if (error != null) {
                    validationErrors.add(error);
                    if (validationErrors.size() >= 10) break; // Limit errors
                }
            }
            if (validationErrors.size() >= 10) break;
        }
        
        if (!validationErrors.isEmpty()) {
            String errorMessage = "Template validation failed. The template file does not meet the field requirements you specified:\n" + 
                                String.join("\n", validationErrors.subList(0, Math.min(5, validationErrors.size())));
            if (validationErrors.size() > 5) {
                errorMessage += "\n... and " + (validationErrors.size() - 5) + " more validation errors";
            }
            errorMessage += "\n\nPlease upload a template file that matches your field configuration or modify your field requirements.";
            throw new RuntimeException(errorMessage);
        }
    }
    
    @Override
    public boolean validateTemplateAgainstFieldConfig(MultipartFile file, String fieldConfig) {
        try {
            String ext = getExt(file.getOriginalFilename());
            byte[] bytes = file.getBytes();
            Object parsed = parseBytes(bytes, ext);
            validateTemplateAgainstFieldConfig(parsed, fieldConfig);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private String validateTemplateField(String fieldName, String value, Map<String, Object> rules, int rowNumber) {
        boolean isRequired = (Boolean) rules.getOrDefault("required", false);
        boolean nullAllowed = (Boolean) rules.getOrDefault("nullAllowed", true);
        boolean specialCharsAllowed = (Boolean) rules.getOrDefault("specialChars", true);
        String fieldType = (String) rules.getOrDefault("fieldType", "string");
        
        System.out.println(String.format("Validating field '%s' with value '%s', type '%s', specialCharsAllowed: %s", fieldName, value, fieldType, specialCharsAllowed));
        
        boolean isEmpty = value == null || value.trim().isEmpty();
        
        // Required field validation
        if (isRequired && isEmpty) {
            return String.format("Row %d: Field '%s' is required but is empty in template", rowNumber, fieldName);
        }
        
        // Null allowed validation
        if (!nullAllowed && isEmpty) {
            return String.format("Row %d: Field '%s' cannot be null but is empty in template", rowNumber, fieldName);
        }
        
        if (isEmpty) {
            return null; // Skip other validations for empty fields
        }
        
        String trimmedValue = value.trim();
        
        // Field type validation
        switch (fieldType.toLowerCase()) {
            case "integer":
                // Check if it's a valid integer format (including Excel format like 1.0)
                if (!trimmedValue.matches("^-?\\d+$") && !trimmedValue.matches("^-?\\d+\\.0+$")) {
                    return String.format("Row %d: Field '%s' must be an integer (whole number) but contains '%s' in template", rowNumber, fieldName, value);
                }
                // Additional check: if it's a decimal like 1.0, ensure it's a whole number
                if (trimmedValue.contains(".")) {
                    try {
                        double doubleValue = Double.parseDouble(trimmedValue);
                        if (doubleValue != Math.floor(doubleValue)) {
                            return String.format("Row %d: Field '%s' must be a whole number but contains '%s' in template", rowNumber, fieldName, value);
                        }
                    } catch (NumberFormatException e) {
                        return String.format("Row %d: Field '%s' must be a valid integer but contains '%s' in template", rowNumber, fieldName, value);
                    }
                }
                break;
            case "decimal":
                if (!trimmedValue.matches("^-?\\d+(\\.\\d+)?$")) {
                    return String.format("Row %d: Field '%s' must be a decimal number but contains '%s' in template", rowNumber, fieldName, value);
                }
                break;
            case "number": // Keep for backward compatibility
                if (!trimmedValue.matches("^-?\\d+(\\.\\d+)?$")) {
                    return String.format("Row %d: Field '%s' must be a number but contains '%s' in template", rowNumber, fieldName, value);
                }
                break;
            case "email":
                if (!trimmedValue.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                    return String.format("Row %d: Field '%s' must be a valid email but contains '%s' in template", rowNumber, fieldName, value);
                }
                break;
            case "date":
                if (!trimmedValue.matches("^\\d{4}-\\d{2}-\\d{2}$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{2}-\\d{2}-\\d{4}$")) {
                    return String.format("Row %d: Field '%s' must be a valid date but contains '%s' in template", rowNumber, fieldName, value);
                }
                break;
        }
        
        // Special characters validation (only when special characters are NOT allowed and field is not numeric/email)
        if (!specialCharsAllowed && !fieldType.toLowerCase().matches("(number|integer|decimal|email)") && trimmedValue.matches(".*[!@#$%^&*()\\[\\]{}|<>?/:;'\"~`].*")) {
            return String.format("Row %d: Field '%s' contains special characters which are not allowed in template", rowNumber, fieldName);
        }
        
        return null;
    }
}
