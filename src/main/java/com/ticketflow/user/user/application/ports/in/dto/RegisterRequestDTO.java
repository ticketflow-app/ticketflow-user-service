package com.ticketflow.user.user.application.ports.in.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ticketflow.user.user.domain.models.enums.UserRole;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegisterRequestDTO(
        String name,
        String email,
        String phone,
        String password,
        UserRole role
) {
    public RegisterRequestDTO(String name, String email, String password, UserRole role) {
        this(name, email, null, password, role);
    }
}
