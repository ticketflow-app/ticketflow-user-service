package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.RefreshTokenRequestDTO;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.SecureTokenPort;
import com.ticketflow.user.user.application.ports.out.TokenProviderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.AccountSuspendedException;
import com.ticketflow.user.user.domain.exception.business.InvalidRefreshTokenException;
import com.ticketflow.user.user.domain.models.RefreshTokenDomain;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshSessionUseCaseTest {

    private RefreshTokenRepositoryPort refreshTokenRepository;
    private UserRepositoryPort userRepository;
    private TokenProviderPort tokenProvider;
    private SecureTokenPort secureTokenPort;
    private RefreshSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepositoryPort.class);
        userRepository = mock(UserRepositoryPort.class);
        tokenProvider = mock(TokenProviderPort.class);
        secureTokenPort = mock(SecureTokenPort.class);
        useCase = new RefreshSessionUseCase(
                refreshTokenRepository, userRepository, tokenProvider, secureTokenPort);
    }

    @Test
    void refresh_validToken_rotatesTokenAndReturnsNewSession() {
        UUID userId = UUID.randomUUID();
        RefreshTokenDomain stored = token(userId, LocalDateTime.now().plusDays(1), null);
        UserDomain user = user(userId, UserStatus.ACTIVE);
        when(secureTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(refreshTokenRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(stored));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(secureTokenPort.generate()).thenReturn("new-raw-token");
        when(secureTokenPort.hash("new-raw-token")).thenReturn("new-token-hash");
        when(tokenProvider.generateToken(userId, "diego@ticketflow.com", UserRole.BUYER))
                .thenReturn("new-access-token");

        LoginResponseDTO result = useCase.refresh(new RefreshTokenRequestDTO("raw-token"));

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.token()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-raw-token");
        assertThat(result.email()).isEqualTo("diego@ticketflow.com");
        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshTokenDomain.class));
    }

    @Test
    void refresh_blankToken_throwsInvalidRefreshToken() {
        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenRequestDTO(" ")))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    @Test
    void refresh_unknownToken_throwsInvalidRefreshToken() {
        when(secureTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(refreshTokenRepository.findByTokenHash("token-hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenRequestDTO("raw-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refresh_revokedToken_revokesEverySession() {
        UUID userId = UUID.randomUUID();
        RefreshTokenDomain stored = token(userId, LocalDateTime.now().plusDays(1), LocalDateTime.now());
        when(secureTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(refreshTokenRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenRequestDTO("raw-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    void refresh_expiredToken_revokesStoredToken() {
        UUID userId = UUID.randomUUID();
        RefreshTokenDomain stored = token(userId, LocalDateTime.now().minusSeconds(1), null);
        when(secureTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(refreshTokenRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenRequestDTO("raw-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refresh_suspendedUser_revokesSessionsAndThrowsAccountSuspended() {
        UUID userId = UUID.randomUUID();
        RefreshTokenDomain stored = token(userId, LocalDateTime.now().plusDays(1), null);
        when(secureTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(refreshTokenRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(stored));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, UserStatus.SUSPENDED)));

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenRequestDTO("raw-token")))
                .isInstanceOf(AccountSuspendedException.class);

        verify(refreshTokenRepository).revokeAllByUserId(userId);
        verify(tokenProvider, never()).generateToken(any(), any(), any());
    }

    private RefreshTokenDomain token(UUID userId, LocalDateTime expiresAt, LocalDateTime revokedAt) {
        return RefreshTokenDomain.from(
                UUID.randomUUID(), userId, "token-hash", expiresAt, revokedAt, LocalDateTime.now());
    }

    private UserDomain user(UUID userId, UserStatus status) {
        return UserDomain.from(
                userId, "Diego", "diego@ticketflow.com", null, "hash", UserRole.BUYER, status,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
