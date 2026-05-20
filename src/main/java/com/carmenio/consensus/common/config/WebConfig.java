package com.carmenio.consensus.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * <p>
 * CORS is handled by {@link SecurityConfig} via Spring Security's
 * {@link org.springframework.web.cors.CorsConfigurationSource}.
 * Virtual threads are configured via {@code spring.threads.virtual.enabled=true}
 * in {@code application.yaml}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Reserved for future MVC-specific configuration
}
