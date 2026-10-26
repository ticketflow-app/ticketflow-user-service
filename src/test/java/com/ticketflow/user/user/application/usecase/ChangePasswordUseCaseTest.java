package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.ChangePasswordRequestDTO;
import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.CurrentPasswordMismatchException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.exception.format.UserPasswordInvalidException;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangePasswordUseCaseTest {

    private UserRepositoryPort userRepository;
    private PasswordEncoderPort passwordEncoder;
    private RefreshTokenRepositoryPort refreshTokenRepository;
    private ChangePasswordUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoderPort.class);
        refreshTokenRepository = mock(RefreshTokenRepositoryPort.class);
        useCase = new ChangePasswordUseCase(userRepository, passwordEncoder, refreshTokenRepository);
    }

    @Test
    void changePassword_validCurrentPassword_updatesHashAndRevokesSessions() {
        UUID userId = UUID.randomUUID();
        UserDomain user = user(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        useCase.changePassword(userId, new ChangePasswordRequestDTO("current-password", "new-password"));

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsMismatch() {
        UUID userId = UUID.randomUUID();
        UserDomain user = user(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> useCase.changePassword(
                userId, new ChangePasswordRequestDTO("wrong", "new-password")))
                .isInstanceOf(CurrentPasswordMismatchException.class);

        verify(userRepository, never()).save(user);
        verify(refreshTokenRepository, never()).revokeAllByUserId(userId);
    }

    @Test
    void changePassword_invalidNewPassword_throwsPasswordInvalid() {
        UUID userId = UUID.randomUUID();
        UserDomain user = user(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> useCase.changePassword(
                userId, new ChangePasswordRequestDTO("current-password", "short")))
                .isInstanceOf(UserPasswordInvalidException.class);

        verify(userRepository, never()).save(user);
    }

    @Test
    void changePassword_missingUser_throwsUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.changePassword(
                userId, new ChangePasswordRequestDTO("current-password", "new-password")))
                .isInstanceOf(UserNotFoundException.class);
    }

    private UserDomain user(UUID userId) {
        return UserDomain.from(
                userId, "Diego", "diego@ticketflow.com", "old-hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
