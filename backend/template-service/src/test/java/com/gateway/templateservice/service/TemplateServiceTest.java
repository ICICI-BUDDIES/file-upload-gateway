package com.gateway.templateservice.service;

import com.gateway.templateservice.model.TemplateEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateServiceTest {

    @Test
    void testTemplateEntityCreation() {
        TemplateEntity entity = new TemplateEntity();
        entity.setId("test-id");
        entity.setCategory("employee");
        entity.setFileType("csv");
        
        assertEquals("test-id", entity.getId());
        assertEquals("employee", entity.getCategory());
        assertEquals("csv", entity.getFileType());
    }
}