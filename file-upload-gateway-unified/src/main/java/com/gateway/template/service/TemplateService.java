package com.gateway.template.service;

import com.gateway.template.dto.RegistrationRequest;
import com.gateway.template.dto.RegistrationResponse;
import com.gateway.template.dto.TemplateMetadataResponse;
import com.gateway.template.dto.TemplateResponse;
import com.gateway.template.model.TemplateEntity;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface TemplateService {
    // New registration methods
    RegistrationResponse registerApp(RegistrationRequest request, MultipartFile templateFile);
    RegistrationResponse registerApp(RegistrationRequest request, MultipartFile templateFile, String headerConfig);
    
    // App-specific template methods
    List<String> getCategoriesByAppHash(String appNameHash);
    TemplateMetadataResponse getMetadataByAppAndCategory(String appNameHash, String category);
    Object getJsonByAppAndCategory(String appNameHash, String category);
    byte[] downloadByAppAndCategory(String appNameHash, String category);
    
    // Legacy methods (keep for backward compatibility)
    @Deprecated
    TemplateResponse uploadTemplate(MultipartFile file, String category, String format, String templateName);
    @Deprecated
    TemplateMetadataResponse getMetadataByCategory(String category);
    @Deprecated
    Object getJsonByCategory(String category);
    @Deprecated
    byte[] downloadByCategoryAndFormat(String category, String format);

    Collection<TemplateEntity> listAll();
    TemplateEntity getById(String id);
    TemplateEntity getByAppAndCategory(String appNameHash, String category);
    void delete(String id);
    boolean pushMetadata(String id, String url);
    List<String> extractHeaders(MultipartFile file);
    boolean validateTemplateAgainstFieldConfig(MultipartFile file, String fieldConfig);
}
