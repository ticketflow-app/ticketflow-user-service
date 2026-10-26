package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.ChangePasswordRequestDTO;

import java.util.UUID;

public interface ChangePasswordPort {
    void changePassword(UUID userId, ChangePasswordRequestDTO request);
}
