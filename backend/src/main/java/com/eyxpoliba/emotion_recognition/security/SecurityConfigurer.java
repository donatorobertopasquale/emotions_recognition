package com.eyxpoliba.emotion_recognition.security;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Slf4j
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfigurer {
    private final JwtAuthFilter jwtAuthFilter;
    private final BlacklistJwtFilter blacklistJwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.sessionManagement(session -> session.sessionCreationPolicy(STATELESS));
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(requests -> {
            requests.requestMatchers("/api/*/public/*").permitAll();
            requests.requestMatchers("/api/login").permitAll();
            requests.requestMatchers("/api/dashboard/").hasRole("ADMIN");
            //requests.requestMatchers("/**/private/**").denyAll();
            requests.anyRequest().permitAll();
        });

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(blacklistJwtFilter, JwtAuthFilter.class);

        return http.build();
    }
}
