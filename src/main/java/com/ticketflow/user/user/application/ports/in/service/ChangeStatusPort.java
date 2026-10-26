package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.enums.UserStatus;

import java.util.UUID;

public interface ChangeStatusPort {
    void execute(UUID actorUserId, UserRole actorRole, UUID targetUserId, UserStatus newStatus);
}
