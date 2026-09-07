package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedRoleChangeException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.UserDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangeRoleUseCaseTest {

    private UserRepositoryPort userRepository;
    private ChangeRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        useCase = new ChangeRoleUseCase(userRepository);
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