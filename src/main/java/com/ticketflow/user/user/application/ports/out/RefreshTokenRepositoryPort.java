package com.ticketflow.user.user.application.ports.out;

import com.ticketflow.user.user.domain.models.RefreshTokenDomain;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {

    RefreshTokenDomain save(RefreshTokenDomain token);

    Optional<RefreshTokenDomain> findByTokenHash(String tokenHash);

    void revoke(RefreshTokenDomain token);

    void revokeAllByUserId(UUID userId);

    void deleteExpiredBefore(LocalDateTime now);
}
