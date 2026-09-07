package com.ticketflow.user.user.application.ports.in.dto;

public record LoginRequestDTO(
        String email,
        String password
) {}