package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestResponseDTO;
import com.ticketflow.user.user.application.ports.in.service.RequestOrganizerPort;
import com.ticketflow.user.user.application.ports.out.OrganizerRequestRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.OrganizerRequestAlreadyExistsException;
import com.ticketflow.user.user.domain.exception.business.OrganizerRequestNotAllowedException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.OrganizerRequestDomain;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestOrganizerUseCase implements RequestOrganizerPort {

    private final UserRepositoryPort userRepository;
    private final OrganizerRequestRepositoryPort organizerRequestRepository;

    @Override
    @Transactional
    public OrganizerRequestResponseDTO request(UUID userId, OrganizerRequestDTO request) {
        UserDomain user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getRole() != UserRole.BUYER) {
            throw new OrganizerRequestNotAllowedException();
        }

        organizerRequestRepository.findPendingByUserId(userId)
                .ifPresent(existing -> {
                    throw new OrganizerRequestAlreadyExistsException();
                });

        validateText(request.company(), "company", 120);
        validateText(request.nit(), "NIT", 40);

        OrganizerRequestDomain saved = organizerRequestRepository.save(
                OrganizerRequestDomain.create(userId, request.company(), request.nit())
        );
        return OrganizerRequestResponseDTO.fromDomain(saved);
    }

    private void validateText(String value, String field, int maxLength) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " is required and must not exceed " + maxLength + " characters.");
        }
    }
}
