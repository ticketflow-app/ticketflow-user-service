package com.ticketflow.user.user.application.ports.in.dto;

import com.ticketflow.user.user.domain.models.OrganizerRequestDomain;
import com.ticketflow.user.user.domain.models.enums.OrganizerRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizerRequestResponseDTO(
        UUID id,
        UUID userId,
        String company,
        String nit,
        OrganizerRequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static OrganizerRequestResponseDTO fromDomain(OrganizerRequestDomain request) {
        return new OrganizerRequestResponseDTO(
                request.getId(),
                request.getUserId(),
                request.getCompany(),
                request.getNit(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getResolvedAt()
        );
    }
}
