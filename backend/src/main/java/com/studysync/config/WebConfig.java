package com.studysync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private String corsAllowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins;
        if (corsAllowedOrigins != null && !corsAllowedOrigins.isBlank()) {
            origins = corsAllowedOrigins.split(",");
        } else {
            origins = new String[]{"http://localhost:*"};
        }

        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Serve the React application at the root URL as well as on client-side routes.
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/subjects").setViewName("forward:/index.html");
        registry.addViewController("/tasks").setViewName("forward:/index.html");
        registry.addViewController("/study-sessions").setViewName("forward:/index.html");
        registry.addViewController("/coding-platforms").setViewName("forward:/index.html");
        registry.addViewController("/analytics").setViewName("forward:/index.html");
    }
}
