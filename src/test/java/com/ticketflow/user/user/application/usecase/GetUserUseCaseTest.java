package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
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
import static org.mockito.Mockito.when;

class GetUserUseCaseTest {

    private UserRepositoryPort userRepository;
    private GetUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        useCase = new GetUserUseCase(userRepository);
    }

    @Test
    void execute_admin_returnsUser() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId)).thenReturn(Optional.of(user(targetId)));

        GetUserResponseDTO result = useCase.execute(UUID.randomUUID(), UserRole.ADMIN, targetId);

        assertThat(result.id()).isEqualTo(targetId);
        assertThat(result.email()).isEqualTo("diego@ticketflow.com");
    }

    @Test
    void execute_nonAdmin_throwsUnauthorizedAdminAction() {
        assertThatThrownBy(() -> useCase.execute(
                UUID.randomUUID(), UserRole.ORGANIZER, UUID.randomUUID()))
                .isInstanceOf(UnauthorizedAdminActionException.class);
    }

    @Test
    void execute_missingUser_throwsUserNotFound() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), UserRole.ADMIN, targetId))
                .isInstanceOf(UserNotFoundException.class);
    }

    private UserDomain user(UUID userId) {
        return UserDomain.from(
                userId, "Diego", "diego@ticketflow.com", "hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
