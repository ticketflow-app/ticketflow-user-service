package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogoutUseCaseTest {

    @Test
    void logout_revokesEveryRefreshTokenForTheUser() {
        RefreshTokenRepositoryPort refreshTokenRepository = mock(RefreshTokenRepositoryPort.class);
        LogoutUseCase useCase = new LogoutUseCase(refreshTokenRepository);
        UUID userId = UUID.randomUUID();

        useCase.logout(userId);

        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
}
