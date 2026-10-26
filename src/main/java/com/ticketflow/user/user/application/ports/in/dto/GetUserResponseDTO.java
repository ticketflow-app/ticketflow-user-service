package com.ticketflow.user.user.application.ports.in.dto;

import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetUserResponseDTO(
        UUID id,
        String name,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt
) {
    public static GetUserResponseDTO fromDomain(UserDomain user) {
        return new GetUserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
