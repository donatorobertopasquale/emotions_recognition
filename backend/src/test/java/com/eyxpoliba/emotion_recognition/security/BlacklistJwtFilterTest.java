package com.eyxpoliba.emotion_recognition.security;

import static org.junit.jupiter.api.Assertions.*;

import com.eyxpoliba.emotion_recognition.repository.BlacklistTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.io.PrintWriter;
import static org.mockito.Mockito.*;

class BlacklistJwtFilterTest {
    @Mock
    private BlacklistTokenRepository blacklistTokenRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private BlacklistJwtFilter blacklistJwtFilter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testDoFilterInternal_LoginPath() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/login");
        blacklistJwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(blacklistTokenRepository);
    }

    @Test
    void testDoFilterInternal_NoCookies() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getCookies()).thenReturn(null);
        blacklistJwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_TokenNotBlacklisted() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure");
        Cookie[] cookies = { new Cookie("ACCESS_TOKEN", "token123") };
        when(request.getCookies()).thenReturn(cookies);
        when(blacklistTokenRepository.existsById("token123")).thenReturn(false);
        blacklistJwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_TokenBlacklisted() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure");
        Cookie[] cookies = { new Cookie("ACCESS_TOKEN", "token123") };
        when(request.getCookies()).thenReturn(cookies);
        when(blacklistTokenRepository.existsById("token123")).thenReturn(true);
        blacklistJwtFilter.doFilterInternal(request, response, filterChain);
    }
}

