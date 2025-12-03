package com.gateway.gateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FieldValidationServiceTest {

    private FieldValidationService service;

    @BeforeEach
    void setUp() {
        service = new FieldValidationService();
    }

    @Test
    void testValidateRequiredField() {
        Map<String, Object> fieldConfig = new HashMap<>();
        Map<String, Object> rules = new HashMap<>();
        rules.put("required", true);
        fieldConfig.put("Name", rules);
        
        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put("Name", "");
        rows.add(row);
        
        List<String> errors = service.validateFields(rows, fieldConfig);
        
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("required"));
    }

    @Test
    void testValidateIntegerField() {
        Map<String, Object> fieldConfig = new HashMap<>();
        Map<String, Object> rules = new HashMap<>();
        rules.put("fieldType", "integer");
        fieldConfig.put("Age", rules);
        
        List<Map<String, String>> rows1 = new ArrayList<>();
        Map<String, String> row1 = new HashMap<>();
        row1.put("Age", "30");
        rows1.add(row1);
        
        List<String> errors1 = service.validateFields(rows1, fieldConfig);
        assertTrue(errors1.isEmpty());
        
        List<Map<String, String>> rows2 = new ArrayList<>();
        Map<String, String> row2 = new HashMap<>();
        row2.put("Age", "abc");
        rows2.add(row2);
        
        List<String> errors2 = service.validateFields(rows2, fieldConfig);
        assertFalse(errors2.isEmpty());
    }

    @Test
    void testValidateDecimalField() {
        Map<String, Object> fieldConfig = new HashMap<>();
        Map<String, Object> rules = new HashMap<>();
        rules.put("fieldType", "decimal");
        fieldConfig.put("Salary", rules);
        
        List<Map<String, String>> rows1 = new ArrayList<>();
        Map<String, String> row1 = new HashMap<>();
        row1.put("Salary", "50000.50");
        rows1.add(row1);
        
        List<String> errors1 = service.validateFields(rows1, fieldConfig);
        assertTrue(errors1.isEmpty());
        
        List<Map<String, String>> rows2 = new ArrayList<>();
        Map<String, String> row2 = new HashMap<>();
        row2.put("Salary", "abc");
        rows2.add(row2);
        
        List<String> errors2 = service.validateFields(rows2, fieldConfig);
        assertFalse(errors2.isEmpty());
    }

    @Test
    void testValidateEmailField() {
        Map<String, Object> fieldConfig = new HashMap<>();
        Map<String, Object> rules = new HashMap<>();
        rules.put("fieldType", "email");
        fieldConfig.put("Email", rules);
        
        List<Map<String, String>> rows1 = new ArrayList<>();
        Map<String, String> row1 = new HashMap<>();
        row1.put("Email", "test@example.com");
        rows1.add(row1);
        
        List<String> errors1 = service.validateFields(rows1, fieldConfig);
        assertTrue(errors1.isEmpty());
        
        List<Map<String, String>> rows2 = new ArrayList<>();
        Map<String, String> row2 = new HashMap<>();
        row2.put("Email", "invalid-email");
        rows2.add(row2);
        
        List<String> errors2 = service.validateFields(rows2, fieldConfig);
        assertFalse(errors2.isEmpty());
    }
}
