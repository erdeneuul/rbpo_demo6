package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Manages JWT token pairs (access + refresh) and user sessions.
 */
@Service
public class TokenPairService {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenPairService(JwtTokenProvider tokenProvider,
                            UserRepository userRepository,
                            UserSessionRepository sessionRepository,
                            PasswordEncoder passwordEncoder) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Login: validate credentials, create session, return token pair.
     */
    @Transactional
    public Map<String, String> login(String username, String password) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Неверный логин или пароль");
        }

        return createTokenPair(user);
    }

    /**
     * Refresh: validate refresh token, invalidate old session, return new token pair.
     * Old refresh token becomes REFRESHED (cannot be reused).
     */
    @Transactional
    public Map<String, String> refresh(String refreshToken) {
        // Step 1: validate JWT signature and expiry
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token недействителен или истёк");
        }

        // Step 2: find session in DB
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Сессия не найдена"));

        // Step 3: check session is still ACTIVE (not already used or revoked)
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException(
                "Refresh token уже был использован или отозван. Статус: " + session.getStatus()
            );
        }

        // Step 4: invalidate old session
        session.setStatus(SessionStatus.REFRESHED);
        sessionRepository.save(session);

        // Step 5: create new token pair
        return createTokenPair(session.getUser());
    }

    /**
     * Creates a new token pair and saves a new session to DB.
     */
    private Map<String, String> createTokenPair(AppUser user) {
        String accessToken = tokenProvider.generateAccessToken(
                user.getUsername(), user.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        // Save new session
        UserSession session = new UserSession(
                user,
                refreshToken,
                LocalDateTime.now(),
                LocalDateTime.now().plusNanos(tokenProvider.getRefreshTokenExpMs() * 1_000_000)
        );
        sessionRepository.save(session);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "tokenType", "Bearer",
                "username", user.getUsername(),
                "role", user.getRole()
        );
    }
}
