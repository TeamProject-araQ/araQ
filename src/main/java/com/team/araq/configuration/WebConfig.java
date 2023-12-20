package com.team.araq.configuration;

import com.team.araq.UserInterceptor;
import com.team.araq.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserService userService;

    @Bean
    public UserInterceptor customUserInterceptor() {
        return new UserInterceptor(userService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customUserInterceptor());
    }

    private String userImagePath= "C:/uploads/user";
}

