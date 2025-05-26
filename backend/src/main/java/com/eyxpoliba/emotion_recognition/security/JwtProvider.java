package com.eyxpoliba.emotion_recognition.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider implements Serializable {
    @Serial
    private static final long serialVersionUID = 234234523523L;

    @Value("${security.secret}")
    private String secretKey;
    @Value("${security.issuer}")
    private String issuer;

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }


    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }


    //for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {

        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }


    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    //generate token for user
    public String generateToken(String username, long userId, boolean isAccess) {
        Map<String, Object> claims = new HashMap<>();
        if (isAccess) claims.put("roles", "USER");
        claims.put("userId", userId);
        return doGenerateToken(claims, username, isAccess);
    }


    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    private String doGenerateToken(Map<String, Object> claims, String subject, boolean isAccess) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(issuer)
                .setExpiration(DateTime.now().plusMinutes(isAccess ? 60 : 120).toDate())
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }


    //validate token
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
