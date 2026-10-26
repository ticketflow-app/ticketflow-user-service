package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.domain.models.enums.UserRole;

import java.util.UUID;

public interface GetUserPort {
    GetUserResponseDTO execute(UUID actorUserId, UserRole actorRole, UUID targetUserId);
}
