package com.example.demo.model;

public enum SessionStatus {
    ACTIVE,
    REFRESHED,  // this session was used to get a new pair (old refresh token invalidated)
    EXPIRED,
    REVOKED
}
