package com.ticketflow.user.user.application.ports.in.dto;

public record ChangePasswordRequestDTO(
        String currentPassword,
        String newPassword
) {}
