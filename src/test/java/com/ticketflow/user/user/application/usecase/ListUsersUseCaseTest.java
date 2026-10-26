package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.common.application.dto.PageResponse;
import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListUsersUseCaseTest {

    private UserRepositoryPort userRepository;
    private ListUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        useCase = new ListUsersUseCase(userRepository);
    }

    @Test
    void execute_admin_returnsPage() {
        UserDomain diego = user("Diego", "diego@ticketflow.com");
        UserDomain ana = user("Ana", "ana@ticketflow.com");
        when(userRepository.search("diego", 0, 10)).thenReturn(List.of(diego, ana));
        when(userRepository.count("diego")).thenReturn(2L);

        PageResponse<GetUserResponseDTO> result = useCase.execute(
                UUID.randomUUID(), UserRole.ADMIN, "  diego  ", 0, 10);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().getFirst().email()).isEqualTo("diego@ticketflow.com");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void execute_nonAdmin_throwsUnauthorizedAdminAction() {
        assertThatThrownBy(() -> useCase.execute(
                UUID.randomUUID(), UserRole.BUYER, "", 0, 10))
                .isInstanceOf(UnauthorizedAdminActionException.class);
    }

    @Test
    void execute_invalidPageAndSize_areClamped() {
        when(userRepository.search("", 0, 100)).thenReturn(List.of());
        when(userRepository.count("")).thenReturn(0L);

        useCase.execute(UUID.randomUUID(), UserRole.ADMIN, null, -5, 999);

        verify(userRepository).search("", 0, 100);
    }

    private UserDomain user(String name, String email) {
        return UserDomain.from(
                UUID.randomUUID(), name, email, "hash", UserRole.BUYER,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
