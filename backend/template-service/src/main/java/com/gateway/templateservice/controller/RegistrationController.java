package com.gateway.templateservice.controller;

import com.gateway.templateservice.dto.RegistrationRequest;
import com.gateway.templateservice.dto.RegistrationResponse;
import com.gateway.templateservice.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/register")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private TemplateService templateService;

    @PostMapping
    public ResponseEntity<RegistrationResponse> registerApp(
            @RequestParam("appName") String appName,
            @RequestParam("category") String category,
            @RequestParam("endpoint") String endpoint,
            @RequestParam("template") MultipartFile templateFile,
            @RequestParam(value = "headerConfig", required = false) String headerConfig) {
        
        RegistrationRequest request = new RegistrationRequest();
        request.setAppName(appName);
        request.setCategory(category);
        request.setEndpoint(endpoint);
        
        RegistrationResponse response = templateService.registerApp(request, templateFile, headerConfig);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/categories/{appNameHash}")
    public ResponseEntity<List<String>> getAppCategories(@PathVariable String appNameHash) {
        try {
            List<String> categories = templateService.getCategoriesByAppHash(appNameHash);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/extract-headers")
    public ResponseEntity<List<String>> extractHeaders(@RequestParam("file") MultipartFile file) {
        try {
            List<String> headers = templateService.extractHeaders(file);
            return ResponseEntity.ok(headers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/validate-template")
    public ResponseEntity<?> validateTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fieldConfig") String fieldConfig) {
        try {
            boolean isValid = templateService.validateTemplateAgainstFieldConfig(file, fieldConfig);
            if (isValid) {
                return ResponseEntity.ok(new ValidationResponse(true, "Template is valid"));
            } else {
                return ResponseEntity.ok(new ValidationResponse(false, "Template validation failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ValidationResponse(false, e.getMessage()));
        }
    }
    
    private static class ValidationResponse {
        private boolean valid;
        private String message;
        
        public ValidationResponse(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}