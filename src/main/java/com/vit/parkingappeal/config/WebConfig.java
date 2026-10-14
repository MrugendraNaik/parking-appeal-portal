package com.vit.parkingappeal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ReviewerAuthInterceptor())
                .addPathPatterns("/reviewer", "/reviewer/**")
                .excludePathPatterns("/reviewer/login");
    }
}
