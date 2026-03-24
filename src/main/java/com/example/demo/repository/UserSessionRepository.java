package com.example.demo.repository;

import com.example.demo.model.SessionStatus;
import com.example.demo.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshToken(String refreshToken);
    void deleteByStatus(SessionStatus status);
}
