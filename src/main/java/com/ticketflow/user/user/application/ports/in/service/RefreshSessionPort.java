package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.RefreshTokenRequestDTO;

public interface RefreshSessionPort {
    LoginResponseDTO refresh(RefreshTokenRequestDTO request);
}
