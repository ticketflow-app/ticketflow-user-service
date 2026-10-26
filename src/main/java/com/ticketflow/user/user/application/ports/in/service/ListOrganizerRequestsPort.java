package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestResponseDTO;
import com.ticketflow.user.user.domain.models.enums.UserRole;

import java.util.List;
import java.util.UUID;

public interface ListOrganizerRequestsPort {
    List<OrganizerRequestResponseDTO> execute(UUID actorUserId, UserRole actorRole);
}
