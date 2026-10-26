package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.CannotChangeOwnStatusException;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangeStatusUseCaseTest {

    private UserRepositoryPort userRepository;
    private RefreshTokenRepositoryPort refreshTokenRepository;
    private ChangeStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        refreshTokenRepository = mock(RefreshTokenRepositoryPort.class);
        useCase = new ChangeStatusUseCase(userRepository, refreshTokenRepository);
    }

    @Test
    void execute_adminSuspendsUser_updatesStatusAndRevokesSessions() {
        UUID targetId = UUID.randomUUID();
        UserDomain user = user(targetId);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));

        useCase.execute(UUID.randomUUID(), UserRole.ADMIN, targetId, UserStatus.SUSPENDED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllByUserId(targetId);
    }

    @Test
    void execute_nonAdmin_throwsUnauthorizedAdminAction() {
        assertThatThrownBy(() -> useCase.execute(
                UUID.randomUUID(), UserRole.BUYER, UUID.randomUUID(), UserStatus.SUSPENDED))
                .isInstanceOf(UnauthorizedAdminActionException.class);
    }

    @Test
    void execute_adminChangesOwnStatus_throwsCannotChangeOwnStatus() {
        UUID adminId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(
                adminId, UserRole.ADMIN, adminId, UserStatus.SUSPENDED))
                .isInstanceOf(CannotChangeOwnStatusException.class);
    }

    @Test
    void execute_missingUser_throwsUserNotFound() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                UUID.randomUUID(), UserRole.ADMIN, targetId, UserStatus.SUSPENDED))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private UserDomain user(UUID userId) {
        return UserDomain.from(
                userId, "Diego", "diego@ticketflow.com", "hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
