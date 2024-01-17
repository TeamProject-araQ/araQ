package com.team.araq.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MVCConfig implements WebMvcConfigurer {
    private String inquiryPath = "uploads/inquiry";

    private String userPath = "uploads/user";

    private String chatPath = "uploads/chat";

    private String audioPath = "uploads/audio";

    private String reviewPath = "uploads/review";

    private String plazaPath = "uploads/plaza";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/inquiry/image/**")
                .addResourceLocations("file:" + inquiryPath + "/");
        registry.addResourceHandler("/user/image/**")
                .addResourceLocations("file:" + userPath + "/");
        registry.addResourceHandler("/chat/image/**")
                .addResourceLocations("file:" + chatPath + "/");
        registry.addResourceHandler("/user/audio/**")
                .addResourceLocations("file:" + audioPath + "/");
        registry.addResourceHandler("/review/image/**")
                .addResourceLocations("file:" + reviewPath + "/");
        registry.addResourceHandler("/plaza/image/**")
                .addResourceLocations("file:" + plazaPath + "/");
    }
}
