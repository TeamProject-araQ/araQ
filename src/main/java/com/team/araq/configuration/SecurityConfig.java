package com.team.araq.configuration;

import com.team.araq.CustomSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.net.URLEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers(new AntPathRequestMatcher("/user/suspended")).hasRole("SUSPEND")
                        .requestMatchers(new AntPathRequestMatcher("/user/banned")).hasRole("BAN")
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAnyRole("ADMIN", "SUPER")
                        .requestMatchers(new AntPathRequestMatcher("/user/**"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/bootstrap**"),
                                new AntPathRequestMatcher("/layout**"),
                                new AntPathRequestMatcher("/araq**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/image/**"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/font/**"),
                                new AntPathRequestMatcher("/ws/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/**")).hasAnyRole("ADMIN", "USER", "SUPER")
                        .anyRequest().authenticated())
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .accessDeniedHandler(new CustomDeniedHandler()))
                .formLogin((formLogin) -> formLogin
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/", true)
                        .successHandler(new CustomSuccessHandler()))
                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true))
                .oauth2Login((oauth2Login) -> oauth2Login
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                AuthenticationException exception) throws IOException {
                                if (exception instanceof OAuth2AuthenticationException) {
                                    OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
                                    if ("email_already_registered".equals(error.getErrorCode())) {
                                        response.sendRedirect("/user/login?error=" + URLEncoder.encode(error.getDescription(), "UTF-8"));
                                    } else {
                                        response.sendRedirect("/user/login?error=authentication_failed");
                                    }
                                } else {
                                    response.sendRedirect("/user/login?error=generic_error");
                                }
                            }
                        })
                );
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
