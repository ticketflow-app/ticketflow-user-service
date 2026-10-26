package com.ticketflow.user.user.infrastructure.adapter.out.persistence.persistence;

import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.domain.models.RefreshTokenDomain;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.entity.RefreshTokenEntity;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenJpaAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshTokenDomain save(RefreshTokenDomain token) {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .tokenHash(token.getTokenHash())
                .expiresAt(token.getExpiresAt())
                .revokedAt(token.getRevokedAt())
                .createdAt(token.getCreatedAt())
                .build();
        return toDomain(refreshTokenRepository.save(entity));
    }

    @Override
    public Optional<RefreshTokenDomain> findByTokenHash(String tokenHash) {
        return refreshTokenRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void revoke(RefreshTokenDomain token) {
        save(token);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    @Override
    public void deleteExpiredBefore(LocalDateTime now) {
        refreshTokenRepository.deleteExpiredBefore(now);
    }

    private RefreshTokenDomain toDomain(RefreshTokenEntity entity) {
        return RefreshTokenDomain.from(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt()
        );
    }
}
