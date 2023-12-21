package com.team.araq.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MVCConfig implements WebMvcConfigurer {
    private String inquiryPath = "C:/uploads/inquiry";

    private String userPath = "C:/uploads/user";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/inquiry/image/**")
                .addResourceLocations("file:" + inquiryPath + "/");
        registry.addResourceHandler("/user/image/**")
                .addResourceLocations("file:" + userPath + "/");
    }
}
