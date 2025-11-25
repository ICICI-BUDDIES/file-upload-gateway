package com.example.gateway.service;

import com.example.gateway.dto.StructureRules;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StructureValidationService {

    public void validateStructure(
            List<String> templateHeaders,
            List<String> actualHeaders,
            StructureRules rules
    ) {
        // 1. Strict column count (if extra not allowed)
        if (!rules.isAllowExtraColumns()) {
            if (templateHeaders.size() != actualHeaders.size()) {
                throw new IllegalArgumentException(
                        String.format("Column count mismatch. Expected %d columns but found %d columns. Please ensure your file has exactly the required columns.", 
                        templateHeaders.size(), actualHeaders.size())
                );
            }
        }

        // 2. Strict or flexible ordering
        if (rules.isStrictColumnOrder()) {
            for (int i = 0; i < templateHeaders.size(); i++) {
                String expected = templateHeaders.get(i).trim();
                String actual = actualHeaders.get(i).trim();

                if (!expected.equalsIgnoreCase(actual)) {
                    throw new IllegalArgumentException(
                            String.format("Header validation failed at column %d. Expected '%s' but found '%s'. Please check your column headers match the template exactly.", 
                            (i + 1), expected, actual)
                    );
                }
            }
        } else {
            // If order not strict, ensure all required headers exist
            for (String expected : templateHeaders) {
                if (!actualHeaders.contains(expected)) {
                    throw new IllegalArgumentException(
                        String.format("Missing required column '%s'. Please add this column to your file.", expected)
                    );
                }
            }
        }
    }

    public void validateRowCount(List<Map<String, Object>> rows, StructureRules rules) {
        int rowCount = rows.size();

        if (rowCount < rules.getMinRows()) {
            throw new IllegalArgumentException(
                    String.format("File has too few data rows. Found %d rows but minimum required is %d rows. Please add more data to your file.", 
                    rowCount, rules.getMinRows())
            );
        }

        if (rowCount > rules.getMaxRows()) {
            throw new IllegalArgumentException(
                    String.format("File has too many data rows. Found %d rows but maximum allowed is %d rows. Please split your data into smaller files.", 
                    rowCount, rules.getMaxRows())
            );
        }
    }
}
