package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.LoginRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;

public interface LoginUserPort {
    LoginResponseDTO login(LoginRequestDTO request);
}