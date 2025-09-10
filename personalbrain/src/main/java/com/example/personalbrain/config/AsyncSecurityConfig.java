package com.example.personalbrain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class AsyncSecurityConfig {


    @Bean("asyncSecurityExecutor")
    public ThreadPoolTaskExecutor asyncSecurityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("async-sec-");
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        
        // Custom task decorator that preserves SecurityContext
        executor.setTaskDecorator(runnable -> {
            var context = SecurityContextHolder.getContext();
            return () -> {
                try {
                    SecurityContextHolder.setContext(context);
                    runnable.run();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };
        });
        
        executor.initialize();
        return executor;
    }
}