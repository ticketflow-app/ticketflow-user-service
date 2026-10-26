package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.UpdateProfileRequestDTO;

import java.util.UUID;

public interface UpdateProfilePort {
    GetUserResponseDTO update(UUID userId, UpdateProfileRequestDTO request);
}
