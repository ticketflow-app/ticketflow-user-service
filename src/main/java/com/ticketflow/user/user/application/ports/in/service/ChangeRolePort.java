package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.domain.models.enums.UserRole;

import java.util.UUID;

public interface ChangeRolePort {
    void execute(UUID actorUserId, UserRole actorRole, UUID targetUserId, UserRole newRole);
}