package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;

import java.util.UUID;

public interface GetMePort {
    GetUserResponseDTO execute(UUID userId);
}