package com.ticketflow.user.user.infrastructure.adapter.out.persistence.repository;

import com.ticketflow.user.user.infrastructure.adapter.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshTokenEntity token set token.revokedAt = :revokedAt " +
            "where token.userId = :userId and token.revokedAt is null")
    int revokeAllByUserId(UUID userId, LocalDateTime revokedAt);

    @Modifying
    @Query("delete from RefreshTokenEntity token where token.expiresAt < :now")
    int deleteExpiredBefore(LocalDateTime now);
}
