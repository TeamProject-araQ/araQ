package com.team.araq;

import com.siot.IamportRestClient.IamportClient;
import com.team.araq.user.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

@Component
public class UserInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public UserInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            Principal principal = request.getUserPrincipal();
            if (principal != null)
                modelAndView.getModelMap().addAttribute("user", userService.getByUsername(principal.getName()));
        }
    }
}
