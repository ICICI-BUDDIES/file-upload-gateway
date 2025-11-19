package com.example.gateway.service;

import com.example.gateway.dto.TemplateDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TemplateLookupService {

    @Value("${template.service.url}")
    private String templateServiceUrl;

    private final RestTemplate rest = new RestTemplate();

    /** --------------------------------------------------------------
     * 1. Fetch ALL template entities from Template-Service
     * -------------------------------------------------------------- */
    public Object fetchAllTemplatesRaw() {
        String url = templateServiceUrl + "/templates";
        return rest.getForObject(url, Object.class);
    }

    /** --------------------------------------------------------------
     * 2. Convert raw template JSON → List<String> categories
     * -------------------------------------------------------------- */
    public List<String> extractCategories(Object raw) {
        if (raw == null) return Collections.emptyList();

        List<String> categories = new ArrayList<>();

        if (raw instanceof List) {
            List<?> list = (List<?>) raw;

            for (Object o : list) {
                if (o instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) o;

                    Object category = m.get("category");
                    if (category != null) {
                        categories.add(String.valueOf(category));
                    }
                }
            }
        }
        return categories;
    }

    /** --------------------------------------------------------------
     * 3. Fetch metadata for a specific template category
     * -------------------------------------------------------------- */
    public TemplateDefinition fetchTemplate(String category) {

        // Correct Template-Service endpoint:
        String url = templateServiceUrl + "/templates/" + category + "/metadata";

        System.out.println("➡ Fetching template metadata from: " + url);

        try {
            return rest.getForObject(url, TemplateDefinition.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch template metadata: " + e.getMessage());
        }
    }
}
