package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestResponseDTO;
import com.ticketflow.user.user.application.ports.in.service.ListOrganizerRequestsPort;
import com.ticketflow.user.user.application.ports.out.OrganizerRequestRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListOrganizerRequestsUseCase implements ListOrganizerRequestsPort {

    private final OrganizerRequestRepositoryPort organizerRequestRepository;

    @Override
    public List<OrganizerRequestResponseDTO> execute(UUID actorUserId, UserRole actorRole) {
        if (actorRole != UserRole.ADMIN) {
            throw new UnauthorizedAdminActionException();
        }
        return organizerRequestRepository.findAll()
                .stream()
                .map(OrganizerRequestResponseDTO::fromDomain)
                .toList();
    }
}
