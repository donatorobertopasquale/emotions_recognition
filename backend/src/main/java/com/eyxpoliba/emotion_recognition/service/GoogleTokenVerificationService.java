package com.eyxpoliba.emotion_recognition.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Service
public class GoogleTokenVerificationService {
    
    private final String googleClientId;
    private final GoogleIdTokenVerifier verifier;
    
    public GoogleTokenVerificationService(@Value("${google.oauth.client-id}") String googleClientId) {
        this.googleClientId = googleClientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), 
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }
    
    public GoogleIdToken.Payload verifyToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            
            // Verify the token's audience
            if (!payload.getAudience().equals(googleClientId)) {
                log.error("Token audience mismatch. Expected: {}, Got: {}", googleClientId, payload.getAudience());
                throw new IllegalArgumentException("Invalid token audience");
            }
            
            // Verify email is verified
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                log.error("Email not verified in Google token");
                throw new IllegalArgumentException("Email not verified");
            }
            
            log.info("Successfully verified Google token for user: {} ({})", payload.getEmail(), payload.getSubject());
            return payload;
        } else {
            log.error("Invalid Google ID token");
            throw new IllegalArgumentException("Invalid Google ID token");
        }
    }
}
