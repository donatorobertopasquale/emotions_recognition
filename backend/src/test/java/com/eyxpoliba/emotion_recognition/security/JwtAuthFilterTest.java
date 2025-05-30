package com.eyxpoliba.emotion_recognition.security;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        // Mock getOutputStream for response

        when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
    }

    @Test
    void testDoFilterInternal_PublicPath() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/public/resource");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_LoginPath() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/login");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getCookies()).thenReturn(null);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(response).setContentType(anyString());
        verify(response).setStatus(eq(401));
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure");
        Cookie[] cookies = { new Cookie("ACCESS_TOKEN", "invalidtoken") };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken("invalidtoken")).thenReturn(false);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(response).setContentType(anyString());
        verify(response).setStatus(eq(401));
    }


    @Test
    void testDoFilterInternal_Exception() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure");
        Cookie[] cookies = { new Cookie("ACCESS_TOKEN", "validtoken") };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken("validtoken")).thenThrow(new RuntimeException("error"));
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(response).setContentType(anyString());
        verify(response).setStatus(eq(401));
    }
}

