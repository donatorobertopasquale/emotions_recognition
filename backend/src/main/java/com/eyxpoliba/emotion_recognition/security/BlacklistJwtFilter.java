package com.eyxpoliba.emotion_recognition.security;

import com.eyxpoliba.emotion_recognition.repository.BlacklistTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import static com.eyxpoliba.emotion_recognition.security.SecurityConstants.ACCESS_TOKEN;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlacklistJwtFilter extends OncePerRequestFilter {
    private final BlacklistTokenRepository blacklistTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("/api/login".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (ACCESS_TOKEN.equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
        if (jwt != null && blacklistTokenRepository.existsById(jwt)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token is blacklisted");
            return;
        }
        filterChain.doFilter(request, response);
    }
}



