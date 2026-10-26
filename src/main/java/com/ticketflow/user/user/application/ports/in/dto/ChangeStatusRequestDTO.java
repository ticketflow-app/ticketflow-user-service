package com.ticketflow.user.user.application.ports.in.dto;

import com.ticketflow.user.user.domain.models.enums.UserStatus;

public record ChangeStatusRequestDTO(
        UserStatus status
) {}
