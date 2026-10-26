package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.UpdateProfileRequestDTO;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateProfileUseCaseTest {

    private UserRepositoryPort userRepository;
    private UpdateProfileUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        useCase = new UpdateProfileUseCase(userRepository);
    }

    @Test
    void update_validRequest_updatesNameAndPhone() {
        UUID userId = UUID.randomUUID();
        UserDomain user = UserDomain.from(
                userId, "Diego", "diego@ticketflow.com", "hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        GetUserResponseDTO result = useCase.update(
                userId, new UpdateProfileRequestDTO("Diego Lopez", "  +57 300 123 4567  "));

        assertThat(result.name()).isEqualTo("Diego Lopez");
        assertThat(result.phone()).isEqualTo("+57 300 123 4567");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(user);
    }

    @Test
    void update_missingUser_throwsUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(
                userId, new UpdateProfileRequestDTO("Diego Lopez", null)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
