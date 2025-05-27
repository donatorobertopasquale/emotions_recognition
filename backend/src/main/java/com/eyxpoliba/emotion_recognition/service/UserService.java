package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.BlacklistTokenEntity;
import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.repository.BlacklistTokenRepository;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import com.eyxpoliba.emotion_recognition.responses.LoginResponse;
import com.eyxpoliba.emotion_recognition.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final AzureStorageService azureStorageService;
    private final JwtProvider jwtProvider;

    public ResponseEntity<LoginResponse> login(UserEntity user, HttpServletResponse response) {
        UserEntity newUser = userRepository.save(user);

        HashMap<String, String> tokens = packJwts(newUser.getNickname(), newUser.getId());
        List<String> imagesName = azureStorageService.getRandomBlobNames(10);

        // Set JWT token as a cookie
        Cookie jwtCookie = new Cookie("accessToken", tokens.get("access_token"));
        //jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour, matching the token expiration
        response.addCookie(jwtCookie);

        Cookie jwtRefreshCookie = new Cookie("refreshToken", tokens.get("refresh_token"));
        //jwtCookie.setHttpOnly(true);
        jwtRefreshCookie.setPath("/");
        jwtRefreshCookie.setMaxAge(120 * 120); // 1 hour, matching the token expiration
        response.addCookie(jwtRefreshCookie);

        return ResponseEntity.created(URI.create("/api/login")).body(new LoginResponse(newUser.getId(), imagesName));
    }

    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();

                    // Clear the cookie
                    Cookie clearCookie = new Cookie("jwt", "");
                    //clearCookie.setHttpOnly(true);
                    clearCookie.setPath("/");
                    clearCookie.setMaxAge(0); // Delete the cookie
                    response.addCookie(clearCookie);

                    break;
                }
            }
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("JWT token is required");
        }
        if (blacklistTokenRepository.existsById(token)) {
            return ResponseEntity.badRequest().body("Already logged out");
        }

        blacklistTokenRepository.save(new BlacklistTokenEntity(token, LocalDate.now()));

        return ResponseEntity.ok("logout successful");
    }


    private HashMap<String, String> packJwts(String username, long userId) {
        String accessToken = jwtProvider.generateToken(username, userId, true);
        String refreshToken = jwtProvider.generateToken(username, userId, false);
        HashMap<String, String> payload = new HashMap<>();
        payload.put("access_token", accessToken);
        payload.put("refresh_token", refreshToken);

        return payload;
    }
}
