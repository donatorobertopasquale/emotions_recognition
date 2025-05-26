package com.eyxpoliba.emotion_recognition.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final ApplicationContext context;

    private static final String BEARER = "Bearer ";


    static public void build401Error(HttpServletResponse response, String message) throws IOException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(UNAUTHORIZED.value());

        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("message", message);

        new ObjectMapper().writeValue(response.getOutputStream(), errorPayload);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (request.getRequestURI().contains("public") || path.equals("/api/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER) || authorizationHeader.length() == BEARER.length()) {
            build401Error(response, "Malformed authorization header");
            return;
        }

        try {
            String token = authorizationHeader.substring(BEARER.length());

            if (!jwtProvider.validateToken(token)) {
                build401Error(response, "Invalid JWT token");
            }
            // Extract claims and set Authentication object
            String username = jwtProvider.getUsernameFromToken(token);
            Long userId = Long.valueOf(jwtProvider.getClaimFromToken(token, claims -> claims.get("userId").toString()));

            // Create authentication token with userId as credentials
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, userId, null);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            build401Error(response, exception.getMessage());
        }
    }
}