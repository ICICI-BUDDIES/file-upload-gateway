package com.gateway.template.service;

import com.gateway.template.dto.RegistrationRequest;
import com.gateway.template.dto.RegistrationResponse;
import com.gateway.template.model.TemplateEntity;
import com.gateway.template.service.impl.TemplateServiceImpl;
import com.gateway.template.storage.TemplateStorage;
import com.gateway.template.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TemplateServiceImplTest {

    @Mock
    private TemplateStorage storage;

    @Mock
    private HashUtil hashUtil;

    @InjectMocks
    private TemplateServiceImpl templateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterAppSuccess() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setAppName("TestApp");
        request.setCategory("invoice");
        request.setEndpoint("http://test.com/api");

        String csvContent = "Name,Age,Email\nJohn,30,john@test.com";
        MockMultipartFile file = new MockMultipartFile(
            "template", "test.csv", "text/csv", csvContent.getBytes()
        );

        when(hashUtil.hashAppName("TestApp")).thenReturn("hash123");
        when(storage.existsByAppHashAndCategory("hash123", "invoice")).thenReturn(false);

        RegistrationResponse response = templateService.registerApp(request, file);

        assertTrue(response.isSuccess());
        assertEquals("hash123", response.getAppNameHash());
        verify(storage, times(1)).save(any(TemplateEntity.class), any(byte[].class));
    }

    @Test
    void testRegisterAppUpdate() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setAppName("TestApp");
        request.setCategory("invoice");
        request.setEndpoint("http://test.com/api");

        String csvContent = "Name,Age\nJohn,30";
        MockMultipartFile file = new MockMultipartFile(
            "template", "test.csv", "text/csv", csvContent.getBytes()
        );

        TemplateEntity existing = new TemplateEntity();
        existing.setId("existing-id");

        when(hashUtil.hashAppName("TestApp")).thenReturn("hash123");
        when(storage.existsByAppHashAndCategory("hash123", "invoice")).thenReturn(true);
        when(storage.findByAppHashAndCategory("hash123", "invoice")).thenReturn(existing);

        RegistrationResponse response = templateService.registerApp(request, file);

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("updated"));
    }

    @Test
    void testExtractHeaders() throws Exception {
        String csvContent = "Name,Age,Email\nJohn,30,john@test.com";
        MockMultipartFile file = new MockMultipartFile(
            "template", "test.csv", "text/csv", csvContent.getBytes()
        );

        List<String> headers = templateService.extractHeaders(file);

        assertEquals(3, headers.size());
        assertTrue(headers.contains("Name"));
        assertTrue(headers.contains("Age"));
        assertTrue(headers.contains("Email"));
    }

    @Test
    void testGetCategoriesByAppHash() {
        when(storage.findByAppHash("hash123")).thenReturn(java.util.Arrays.asList(
            createTemplateEntity("invoice"),
            createTemplateEntity("employee")
        ));

        List<String> categories = templateService.getCategoriesByAppHash("hash123");

        assertEquals(2, categories.size());
        assertTrue(categories.contains("invoice"));
        assertTrue(categories.contains("employee"));
    }

    private TemplateEntity createTemplateEntity(String category) {
        TemplateEntity entity = new TemplateEntity();
        entity.setCategory(category);
        return entity;
    }
}
