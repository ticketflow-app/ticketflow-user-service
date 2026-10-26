package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.service.LogoutPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogoutUseCase implements LogoutPort {

    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Override
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
