package com.eyxpoliba.emotion_recognition.security;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.mockito.Mockito.*;

class SecurityConfigurerTest {
    @Mock
    private JwtAuthFilter jwtAuthFilter;
    @Mock
    private BlacklistJwtFilter blacklistJwtFilter;

    @InjectMocks
    private SecurityConfigurer securityConfigurer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSecurityFilterChain() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        // Mock chained methods
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        //when(httpSecurity.build()).thenReturn(mock(SecurityFilterChain.class));

        SecurityFilterChain chain = securityConfigurer.securityFilterChain(httpSecurity);
        assertNotNull(chain);
        // Verify filters are added
        verify(httpSecurity, times(1)).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        verify(httpSecurity, times(1)).addFilterBefore(blacklistJwtFilter, JwtAuthFilter.class);
    }
}
