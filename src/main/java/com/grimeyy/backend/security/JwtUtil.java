package com.grimeyy.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.grimeyy.backend.user.User;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "MeinGeheimerJwtSchluesselMeinGeheimerJwtSchluessel";
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24; // 1 Day
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 Days

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public String generateAccessToken(String email) {
        return generateToken(email, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, REFRESH_TOKEN_EXPIRATION);
    }

    private String generateToken(String email, long expirationMillis) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT Validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateTokenForUser(String token, User user) {
        return validateToken(token) &&
               extractEmail(token).equals(user.getEmail()) &&
               !isTokenExpired(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return validateToken(token) &&
               extractEmail(token).equals(userDetails.getUsername()) &&
               !isTokenExpired(token);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return parseToken(token).getBody();
        } catch (JwtException e) {
            logger.error("Failed to extract claims: {}", e.getMessage());
            throw e;
        }
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}

