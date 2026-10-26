package com.ticketflow.user.user.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class RefreshTokenDomain {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private final LocalDateTime createdAt;

    private RefreshTokenDomain(UUID id, UUID userId, String tokenHash, LocalDateTime expiresAt,
                               LocalDateTime revokedAt, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "ID is mandatory");
        this.userId = Objects.requireNonNull(userId, "User ID is mandatory");
        this.tokenHash = Objects.requireNonNull(tokenHash, "Token hash is mandatory");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration is mandatory");
        this.revokedAt = revokedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is mandatory");
    }

    public static RefreshTokenDomain create(UUID userId, String tokenHash, LocalDateTime expiresAt) {
        return new RefreshTokenDomain(UUID.randomUUID(), userId, tokenHash, expiresAt, null, LocalDateTime.now());
    }

    public static RefreshTokenDomain from(UUID id, UUID userId, String tokenHash, LocalDateTime expiresAt,
                                          LocalDateTime revokedAt, LocalDateTime createdAt) {
        return new RefreshTokenDomain(id, userId, tokenHash, expiresAt, revokedAt, createdAt);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
