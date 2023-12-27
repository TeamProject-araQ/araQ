package com.team.araq.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MVCConfig implements WebMvcConfigurer {
    private String inquiryPath = "C:/uploads/inquiry";

    private String userPath = "C:/uploads/user";

    private String chatPath = "C:/uploads/chat";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/inquiry/image/**")
                .addResourceLocations("file:" + inquiryPath + "/");
        registry.addResourceHandler("/user/image/**")
                .addResourceLocations("file:" + userPath + "/");
        registry.addResourceHandler("/chat/image/**")
                .addResourceLocations("file:" + chatPath + "/");
    }
}
