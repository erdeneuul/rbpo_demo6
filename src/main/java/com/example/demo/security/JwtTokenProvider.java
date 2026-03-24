package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Handles creation and validation of JWT access and refresh tokens.
 *
 * Access token:  short-lived (15 min), used to access protected endpoints
 * Refresh token: long-lived (7 days), used only to get a new token pair
 */
@Component
public class JwtTokenProvider {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    // Access token: 15 minutes
    private final long accessTokenExpMs = 15 * 60 * 1000L;

    // Refresh token: 7 days
    private final long refreshTokenExpMs = 7 * 24 * 60 * 60 * 1000L;

    public JwtTokenProvider(
            @Value("${jwt.access-secret:accessSecretKeyForRBPO2025DemoProjectLongEnough}") String accessSecret,
            @Value("${jwt.refresh-secret:refreshSecretKeyForRBPO2025DemoProjectLongEnough}") String refreshSecret) {
        this.accessKey = Keys.hmacShaKeyFor(
                Base64.getEncoder().encode(accessSecret.getBytes()));
        this.refreshKey = Keys.hmacShaKeyFor(
                Base64.getEncoder().encode(refreshSecret.getBytes()));
    }

    // ─── ACCESS TOKEN ───────────────────────────────────────────

    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpMs))
                .signWith(accessKey)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromAccessToken(String token) {
        return Jwts.parser().verifyWith(accessKey).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    // ─── REFRESH TOKEN ──────────────────────────────────────────

    public String generateRefreshToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpMs))
                .signWith(refreshKey)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromRefreshToken(String token) {
        return Jwts.parser().verifyWith(refreshKey).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public long getRefreshTokenExpMs() {
        return refreshTokenExpMs;
    }
}
