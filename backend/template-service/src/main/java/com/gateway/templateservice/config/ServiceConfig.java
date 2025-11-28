package com.gateway.templateservice.config;

import com.gateway.templateservice.service.TemplateService;
import com.gateway.templateservice.service.impl.TemplateServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    
    @Bean
    public TemplateService templateService() {
        return new TemplateServiceImpl();
    }
}