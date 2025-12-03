package com.gateway.gateway.service;

import com.gateway.template.storage.TemplateStorage;
import com.gateway.template.model.TemplateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EndpointNotificationService {

    @Autowired
    private TemplateStorage templateStorage;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendDataToAppEndpoint(String appNameHash, String category, List<Map<String, Object>> extractedData) {
        try {
            String endpointUrl = getAppEndpoint(appNameHash, category);
            
            if (endpointUrl == null || endpointUrl.trim().isEmpty()) {
                System.err.println("No endpoint registered for app: " + appNameHash + ", category: " + category);
                return false;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(extractedData, headers);
            
            System.out.println("📤 Sending data to app endpoint: " + endpointUrl);
            System.out.println("📊 Data: " + extractedData.size() + " records");
            
            restTemplate.postForObject(endpointUrl, request, String.class);
            
            System.out.println("✅ Successfully sent data to app endpoint");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send data to app endpoint: " + e.getMessage());
            return false;
        }
    }
    
    private String getAppEndpoint(String appNameHash, String category) {
        try {
            TemplateEntity template = templateStorage.findByAppHashAndCategory(appNameHash, category);
            return template != null ? template.getEndpoint() : null;
        } catch (Exception e) {
            System.err.println("Failed to get app endpoint: " + e.getMessage());
            return null;
        }
    }
}
