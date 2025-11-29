package com.gateway.gateway.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class FieldValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+(\\.0+)?$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{2}-\\d{2}-\\d{4}$");
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[!@#$%^&*()\\[\\]{}|<>?/:;'\"~`]");
    
    private static final int MAX_ERRORS = 50;

    public List<String> validateFields(List<Map<String, String>> rows, Map<String, Object> fieldConfig) {
        if (fieldConfig == null || fieldConfig.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> errors = new ArrayList<>();
        
        // Process all rows with optimized validation
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            if (errors.size() >= MAX_ERRORS) {
                errors.add(String.format("Validation stopped at row %d. Too many errors found. Fix the first %d errors and re-upload.", rowIndex + 1, MAX_ERRORS));
                break;
            }
            
            Map<String, String> row = rows.get(rowIndex);
            
            for (Map.Entry<String, Object> configEntry : fieldConfig.entrySet()) {
                String fieldName = configEntry.getKey();
                Map<String, Object> fieldRules = (Map<String, Object>) configEntry.getValue();
                
                String fieldValue = row.get(fieldName);
                String error = validateFieldOptimized(fieldName, fieldValue, fieldRules, rowIndex + 1);
                
                if (error != null) {
                    errors.add(error);
                    if (errors.size() >= MAX_ERRORS) break;
                }
            }
        }
        
        return errors;
    }

    private String validateFieldOptimized(String fieldName, String value, Map<String, Object> rules, int rowNumber) {
        boolean isRequired = (Boolean) rules.getOrDefault("required", false);
        boolean nullAllowed = (Boolean) rules.getOrDefault("nullAllowed", true);
        boolean specialCharsAllowed = (Boolean) rules.getOrDefault("specialChars", true);
        String fieldType = (String) rules.getOrDefault("fieldType", "string");
        
        boolean isEmpty = value == null || value.trim().isEmpty();
        
        // Required field validation
        if (isRequired && isEmpty) {
            return String.format("Row %d: Field '%s' is required but is empty", rowNumber, fieldName);
        }
        
        // Null allowed validation
        if (!nullAllowed && isEmpty) {
            return String.format("Row %d: Field '%s' cannot be null or empty", rowNumber, fieldName);
        }
        
        // Skip validation for empty fields (if allowed)
        if (isEmpty) {
            return null;
        }
        
        String trimmedValue = value.trim();
        
        // Field type validation (optimized with single pattern match)
        switch (fieldType.toLowerCase()) {
            case "integer":
                if (!INTEGER_PATTERN.matcher(trimmedValue).matches()) {
                    return String.format("Row %d: Field '%s' must be a valid integer (whole number), got '%s'", rowNumber, fieldName, value);
                }
                // Additional check: if it's a decimal like 1.0, ensure it's a whole number
                if (trimmedValue.contains(".")) {
                    try {
                        double doubleValue = Double.parseDouble(trimmedValue);
                        if (doubleValue != Math.floor(doubleValue)) {
                            return String.format("Row %d: Field '%s' must be a whole number, got '%s'", rowNumber, fieldName, value);
                        }
                    } catch (NumberFormatException e) {
                        return String.format("Row %d: Field '%s' must be a valid integer, got '%s'", rowNumber, fieldName, value);
                    }
                }
                break;
            case "decimal":
                if (!DECIMAL_PATTERN.matcher(trimmedValue).matches()) {
                    return String.format("Row %d: Field '%s' must be a valid decimal number, got '%s'", rowNumber, fieldName, value);
                }
                break;
            case "number": // Keep for backward compatibility
                if (!INTEGER_PATTERN.matcher(trimmedValue).matches() && !DECIMAL_PATTERN.matcher(trimmedValue).matches()) {
                    return String.format("Row %d: Field '%s' must be a valid number, got '%s'", rowNumber, fieldName, value);
                }
                break;
            case "email":
                if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
                    return String.format("Row %d: Field '%s' must be a valid email address, got '%s'", rowNumber, fieldName, value);
                }
                break;
            case "date":
                if (!DATE_PATTERN.matcher(trimmedValue).matches()) {
                    return String.format("Row %d: Field '%s' must be a valid date (YYYY-MM-DD, MM/DD/YYYY, or MM-DD-YYYY), got '%s'", rowNumber, fieldName, value);
                }
                break;
        }
        
        // Special characters validation (skip for numeric and email fields)
        if (!specialCharsAllowed && !fieldType.toLowerCase().matches("(integer|decimal|number|email)") && SPECIAL_CHARS_PATTERN.matcher(trimmedValue).find()) {
            return String.format("Row %d: Field '%s' contains special characters which are not allowed", rowNumber, fieldName);
        }
        
        return null;
    }
}
