package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestResponseDTO;
import com.ticketflow.user.user.application.ports.out.OrganizerRequestRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.OrganizerRequestAlreadyExistsException;
import com.ticketflow.user.user.domain.exception.business.OrganizerRequestNotAllowedException;
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

class RequestOrganizerUseCaseTest {

    private UserRepositoryPort userRepository;
    private OrganizerRequestRepositoryPort organizerRequestRepository;
    private RequestOrganizerUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        organizerRequestRepository = mock(OrganizerRequestRepositoryPort.class);
        useCase = new RequestOrganizerUseCase(userRepository, organizerRequestRepository);
    }

    @Test
    void request_buyerWithoutPendingRequest_createsPendingRequest() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, UserRole.BUYER)));
        when(organizerRequestRepository.findPendingByUserId(userId)).thenReturn(Optional.empty());
        when(organizerRequestRepository.save(any(OrganizerRequestDomain.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrganizerRequestResponseDTO result = useCase.request(
                userId, new OrganizerRequestDTO("  TicketFlow SAS  ", " 900123456-7 "));

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.company()).isEqualTo("TicketFlow SAS");
        assertThat(result.nit()).isEqualTo("900123456-7");
        assertThat(result.status()).isEqualTo(OrganizerRequestStatus.PENDING);
        verify(organizerRequestRepository).save(any(OrganizerRequestDomain.class));
    }

    @Test
    void request_userAlreadyOrganizer_throwsNotAllowed() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, UserRole.ORGANIZER)));

        assertThatThrownBy(() -> useCase.request(
                userId, new OrganizerRequestDTO("TicketFlow SAS", "900123456-7")))
                .isInstanceOf(OrganizerRequestNotAllowedException.class);

        verify(organizerRequestRepository, never()).save(any());
    }

    @Test
    void request_pendingRequestAlreadyExists_throwsAlreadyExists() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, UserRole.BUYER)));
        when(organizerRequestRepository.findPendingByUserId(userId))
                .thenReturn(Optional.of(OrganizerRequestDomain.create(userId, "Old", "1")));

        assertThatThrownBy(() -> useCase.request(
                userId, new OrganizerRequestDTO("TicketFlow SAS", "900123456-7")))
                .isInstanceOf(OrganizerRequestAlreadyExistsException.class);

        verify(organizerRequestRepository, never()).save(any());
    }

    @Test
    void request_blankCompany_throwsIllegalArgument() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, UserRole.BUYER)));
        when(organizerRequestRepository.findPendingByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.request(
                userId, new OrganizerRequestDTO("   ", "900123456-7")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void request_missingUser_throwsUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.request(
                userId, new OrganizerRequestDTO("TicketFlow SAS", "900123456-7")))
                .isInstanceOf(UserNotFoundException.class);
    }

    private UserDomain user(UUID userId, UserRole role) {
        return UserDomain.from(
                userId, "Diego", "diego@ticketflow.com", "hash", role,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
