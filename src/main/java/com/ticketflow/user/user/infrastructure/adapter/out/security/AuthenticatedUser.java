package com.ticketflow.user.user.infrastructure.adapter.out.security;

import com.ticketflow.user.user.domain.models.enums.UserRole;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email,
        UserRole role
) {}