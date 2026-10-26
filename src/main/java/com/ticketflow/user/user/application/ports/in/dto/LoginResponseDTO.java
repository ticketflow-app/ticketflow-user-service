package com.ticketflow.user.user.application.ports.in.dto;

import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.enums.UserStatus;

import java.util.UUID;

public record LoginResponseDTO(
        String token,
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID id,
        String name,
        String email,
        String phone,
        UserRole role,
        UserStatus status
) {
    public static LoginResponseDTO from(String accessToken, String refreshToken, UserDomain user) {
        return new LoginResponseDTO(
                accessToken,
                accessToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus()
        );
    }
}
