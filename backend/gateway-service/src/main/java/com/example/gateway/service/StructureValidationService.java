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
                        "Column count mismatch. Expected " + templateHeaders.size() +
                                ", got " + actualHeaders.size()
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
                            "Header mismatch at column " + (i + 1) +
                                    ". Expected: '" + expected + "', got: '" + actual + "'"
                    );
                }
            }
        } else {
            // If order not strict, ensure all required headers exist
            for (String expected : templateHeaders) {
                if (!actualHeaders.contains(expected)) {
                    throw new IllegalArgumentException("Missing required column: " + expected);
                }
            }
        }
    }

    public void validateRowCount(List<Map<String, Object>> rows, StructureRules rules) {
        int rowCount = rows.size();

        if (rowCount < rules.getMinRows()) {
            throw new IllegalArgumentException(
                    "Row count too small. Minimum allowed: " + rules.getMinRows()
            );
        }

        if (rowCount > rules.getMaxRows()) {
            throw new IllegalArgumentException(
                    "Row count too large. Maximum allowed: " + rules.getMaxRows()
            );
        }
    }
}
