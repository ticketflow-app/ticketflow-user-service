package com.ticketflow.user.user.application.ports.in.dto;

import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.UserDomain;

import java.util.UUID;

public record LoginResponseDTO(
        String token,
        UUID id,
        String name,
        String email,
        UserRole role
) {
    public static LoginResponseDTO from(String token, UserDomain user) {
        return new LoginResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}