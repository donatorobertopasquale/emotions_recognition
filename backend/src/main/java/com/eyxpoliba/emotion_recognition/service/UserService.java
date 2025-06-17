package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.dto.GoogleLoginRequest;
import com.eyxpoliba.emotion_recognition.model.BlacklistTokenEntity;
import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.repository.BlacklistTokenRepository;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import com.eyxpoliba.emotion_recognition.responses.LoginResponse;
import com.eyxpoliba.emotion_recognition.security.JwtProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.eyxpoliba.emotion_recognition.security.SecurityConstants.ACCESS_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final AzureStorageService azureStorageService;
    private final JwtProvider jwtProvider;
    private final GoogleTokenVerificationService googleTokenVerificationService;

    public ResponseEntity<LoginResponse> login(UserEntity user, HttpServletResponse response) {
        UserEntity newUser = userRepository.save(user);

        HashMap<String, String> tokens = packJwts(newUser.getNickname(), newUser.getId());
        List<String> imagesName = azureStorageService.getRandomBlobNames(10);

        // Set JWT token as a cookie
        Cookie jwtCookie = new Cookie(ACCESS_TOKEN, tokens.get("access_token"));
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

    public ResponseEntity<LoginResponse> googleLogin(GoogleLoginRequest request, HttpServletResponse response) {
        try {
            log.info("Processing Google login request for user with nickname: {}", request.getNickname());
            
            // Verify Google JWT token
            GoogleIdToken.Payload payload = googleTokenVerificationService.verifyToken(request.getGoogleCredential());
            
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            Boolean emailVerified = payload.getEmailVerified();
            
            log.info("Google token verified for user: {} ({})", email, googleId);
            
            // Find or create user by Google ID
            Optional<UserEntity> existingUser = userRepository.findByGoogleId(googleId);
            UserEntity user;
            
            if (existingUser.isPresent()) {
                // User exists, update their information
                user = existingUser.get();
                user.setNickname(request.getNickname());
                user.setAge(request.getAge());
                user.setGender(request.getGender());
                user.setNationality(request.getNationality());
                user.setEmail(email);
                user.setEmailVerified(emailVerified);
                
                log.info("Updating existing user: {}", user.getId());
            } else {
                // Create new user
                user = new UserEntity();
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setNickname(request.getNickname());
                user.setAge(request.getAge());
                user.setGender(request.getGender());
                user.setNationality(request.getNationality());
                user.setEmailVerified(emailVerified);
                
                log.info("Creating new user with Google ID: {}", googleId);
            }
            
            // Save user to database
            UserEntity savedUser = userRepository.save(user);
            log.info("User saved with ID: {}", savedUser.getId());
            
            // Generate JWT tokens for API access
            HashMap<String, String> tokens = packJwts(savedUser.getNickname(), savedUser.getId());
            List<String> imagesName = azureStorageService.getRandomBlobNames(10);
            
            // Set JWT tokens as cookies
            Cookie jwtCookie = new Cookie(ACCESS_TOKEN, tokens.get("access_token"));
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60); // 1 hour
            response.addCookie(jwtCookie);
            
            Cookie jwtRefreshCookie = new Cookie("refreshToken", tokens.get("refresh_token"));
            jwtRefreshCookie.setPath("/");
            jwtRefreshCookie.setMaxAge(120 * 120); // 2 hours
            response.addCookie(jwtRefreshCookie);
            
            log.info("Google login successful for user: {}", savedUser.getNickname());
            return ResponseEntity.created(URI.create("/api/google-login"))
                    .body(new LoginResponse(savedUser.getId(), imagesName));
                    
        } catch (GeneralSecurityException | IOException e) {
            log.error("Google token verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Unexpected error during Google login: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN.equals(cookie.getName())) {
                    token = cookie.getValue();

                    // Clear the cookie
                    Cookie clearCookie = new Cookie(ACCESS_TOKEN, "");
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
