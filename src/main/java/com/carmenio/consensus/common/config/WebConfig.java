package com.carmenio.consensus.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * <p>
 * Configures CORS, and other web-related settings.
 * Virtual threads are configured via {@code spring.threads.virtual.enabled=true}
 * in {@code application.yaml}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final long MAX_AGE_SECS = 3600L;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(MAX_AGE_SECS);
    }
}
