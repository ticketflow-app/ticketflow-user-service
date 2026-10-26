package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.out.OrganizerRequestRepositoryPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedRoleChangeException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.OrganizerRequestDomain;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.OrganizerRequestStatus;
import com.ticketflow.user.user.domain.models.enums.UserRole;
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

class ChangeRoleUseCaseTest {

    private UserRepositoryPort userRepository;
    private RefreshTokenRepositoryPort refreshTokenRepository;
    private OrganizerRequestRepositoryPort organizerRequestRepository;
    private ChangeRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        refreshTokenRepository = mock(RefreshTokenRepositoryPort.class);
        organizerRequestRepository = mock(OrganizerRequestRepositoryPort.class);
        useCase = new ChangeRoleUseCase(userRepository, refreshTokenRepository, organizerRequestRepository);
    }

    @Test
    void execute_adminActor_changesRole() {
        UUID targetId = UUID.randomUUID();
        UserDomain user = UserDomain.from(
                targetId, "Diego", "diego@ticketflow.com", "hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));

        useCase.execute(UUID.randomUUID(), UserRole.ADMIN, targetId, UserRole.STAFF_CHECKIN);

        assertThat(user.getRole()).isEqualTo(UserRole.STAFF_CHECKIN);
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllByUserId(targetId);
    }

    @Test
    void execute_promoteToOrganizer_approvesPendingRequest() {
        UUID targetId = UUID.randomUUID();
        UserDomain user = UserDomain.from(
                targetId, "Diego", "diego@ticketflow.com", "hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
        OrganizerRequestDomain pending = OrganizerRequestDomain.create(
                targetId, "TicketFlow SAS", "900123456-7");
        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));
        when(organizerRequestRepository.findPendingByUserId(targetId)).thenReturn(Optional.of(pending));

        useCase.execute(UUID.randomUUID(), UserRole.ADMIN, targetId, UserRole.ORGANIZER);

        assertThat(user.getRole()).isEqualTo(UserRole.ORGANIZER);
        assertThat(pending.getStatus()).isEqualTo(OrganizerRequestStatus.APPROVED);
        verify(organizerRequestRepository).save(pending);
    }

    @Test
    void execute_changeToAnotherRole_leavesPendingRequestUntouched() {
        UUID targetId = UUID.randomUUID();
        UserDomain user = UserDomain.from(
                targetId, "Diego", "diego@ticketflow.com", "hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));

        useCase.execute(UUID.randomUUID(), UserRole.ADMIN, targetId, UserRole.STAFF_CHECKIN);

        verify(organizerRequestRepository, never()).save(any());
    }

    @Test
    void execute_nonAdminActor_throwsUnauthorizedRoleChange() {
        assertThatThrownBy(() -> useCase.execute(
                UUID.randomUUID(), UserRole.BUYER, UUID.randomUUID(), UserRole.ORGANIZER))
                .isInstanceOf(UnauthorizedRoleChangeException.class);
    }

    @Test
    void execute_targetUserNotFound_throwsUserNotFound() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                UUID.randomUUID(), UserRole.ADMIN, targetId, UserRole.ORGANIZER))
                .isInstanceOf(UserNotFoundException.class);
    }
}
