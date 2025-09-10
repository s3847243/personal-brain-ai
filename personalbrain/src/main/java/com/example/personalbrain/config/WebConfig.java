package com.example.personalbrain.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;
    // Toggle this if you’re NOT proxying and are truly cross-site (requires HTTPS, SameSite=None; Secure)
    private static final boolean SAME_ORIGIN_DEV = true;
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        if (SAME_ORIGIN_DEV) {
            // Using Next proxy => requests appear same-origin, but allowing localhost is fine
            config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
            config.setAllowCredentials(true);
        } else {
            // Cross-site: set exact front-end origin, keep allowCredentials=true
            config.setAllowedOrigins(List.of("https://localhost:3000"));
            config.setAllowCredentials(true);
        }

        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type","Authorization","X-Requested-With"));
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}