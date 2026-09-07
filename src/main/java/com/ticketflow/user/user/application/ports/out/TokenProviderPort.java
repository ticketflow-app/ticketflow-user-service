package com.ticketflow.user.user.application.ports.out;

import com.ticketflow.user.user.domain.models.enums.UserRole;

import java.util.UUID;

public interface TokenProviderPort {
    String generateToken(UUID userId, String email, UserRole role);
}