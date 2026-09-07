package com.ticketflow.user.user.infrastructure.adapter.in.controller;

import com.ticketflow.user.common.application.dto.ApiResponse;
import com.ticketflow.user.user.application.ports.in.service.LoginUserPort;
import com.ticketflow.user.user.application.ports.in.service.RegisterUserPort;
import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.LoginRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.RegisterRequestDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints for registration and authentication")
public class AuthController {

    private final RegisterUserPort registerUserPort;
    private final LoginUserPort loginUserPort;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<GetUserResponseDTO>> register(
            @RequestBody RegisterRequestDTO request) {
        GetUserResponseDTO user = registerUserPort.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = loginUserPort.login(request);
        return ResponseEntity
                .ok(ApiResponse.success(response, "Login successful"));
    }
}