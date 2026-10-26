package com.ticketflow.user.user.application.ports.in.dto;

public record UpdateProfileRequestDTO(
        String name,
        String phone
) {}
