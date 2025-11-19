package com.gateway.templateservice.service;

import com.gateway.templateservice.dto.TemplateMetadataResponse;
import com.gateway.templateservice.dto.TemplateResponse;
import com.gateway.templateservice.model.TemplateEntity;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface TemplateService {
    TemplateResponse uploadTemplate(MultipartFile file, String category, String format, String templateName);

    Collection<TemplateEntity> listAll();

    TemplateEntity getById(String id);

    void delete(String id);

    TemplateMetadataResponse getMetadataByCategory(String category);

    Object getJsonByCategory(String category);

    byte[] downloadByCategoryAndFormat(String category, String format);

    boolean pushMetadata(String id, String url);
}
