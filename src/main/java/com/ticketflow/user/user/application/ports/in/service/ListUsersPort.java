package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.common.application.dto.PageResponse;
import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.domain.models.enums.UserRole;

import java.util.UUID;

public interface ListUsersPort {
    PageResponse<GetUserResponseDTO> execute(UUID actorUserId, UserRole actorRole, String query, int page, int size);
}
