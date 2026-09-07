package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.models.enums.UserRole;
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
import static org.mockito.Mockito.when;

class GetMeUseCaseTest {

    private UserRepositoryPort userRepository;
    private GetMeUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        useCase = new GetMeUseCase(userRepository);
    }

    @Test
    void execute_existingUser_returnsUser() {
        UUID id = UUID.randomUUID();
        UserDomain user = UserDomain.from(
                id, "Diego", "diego@ticketflow.com", "hash", UserRole.ORGANIZER,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        GetUserResponseDTO result = useCase.execute(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Diego");
        assertThat(result.role()).isEqualTo(UserRole.ORGANIZER);
    }

    @Test
    void execute_unknownUser_throwsUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id)).isInstanceOf(UserNotFoundException.class);
    }
}