package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.RegisterRequestDTO;

public interface RegisterUserPort {
    GetUserResponseDTO register(RegisterRequestDTO request);
}