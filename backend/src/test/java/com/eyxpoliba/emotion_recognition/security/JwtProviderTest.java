package com.eyxpoliba.emotion_recognition.security;

import static org.junit.jupiter.api.Assertions.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Date;

class JwtProviderTest {
    private JwtProvider jwtProvider;
    private final String secretKey = "testSecretKey12345678901234567890123456789012";
    private final String issuer = "testIssuer";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtProvider, "issuer", issuer);
    }

    @Test
    void testGenerateAndParseToken() {
        String username = "testuser";
        long userId = 42L;
        boolean isAccess = true;
        String token = jwtProvider.generateToken(username, userId, isAccess);
        assertNotNull(token);
        assertEquals(username, jwtProvider.getUsernameFromToken(token));
        assertEquals(userId, (Long) jwtProvider.getClaimFromToken(token, claims -> claims.get("userId", Long.class)));
        assertEquals("USER", jwtProvider.getClaimFromToken(token, claims -> claims.get("roles", String.class)));
        assertEquals(issuer, Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getIssuer());
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        String username = "testuser";
        long userId = 1L;
        // Generate a token with a short expiration for testing
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setExpiration(new Date(System.currentTimeMillis() + 1000)) // 1 second
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
        assertTrue(jwtProvider.validateToken(token));
        Thread.sleep(1000000); // Wait for token to expire
        assertFalse(jwtProvider.validateToken(token));
    }

    @Test
    void testGetExpirationDateFromToken() {
        String username = "testuser";
        Date expiration = new Date(System.currentTimeMillis() + 60000);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
        Date extracted = jwtProvider.getExpirationDateFromToken(token);
        assertNotNull(extracted);
        assertEquals(expiration.getTime(), extracted.getTime(), 1000); // Allow 1s difference
    }
}

