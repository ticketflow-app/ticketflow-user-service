package com.ticketflow.user.user.infrastructure.adapter.in.controller;

import com.ticketflow.user.common.application.dto.ApiResponse;
import com.ticketflow.user.user.application.ports.in.service.ChangeRolePort;
import com.ticketflow.user.user.application.ports.in.service.GetMePort;
import com.ticketflow.user.user.application.ports.in.dto.ChangeRoleRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.infrastructure.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for authenticated user information and administration")
public class UserController {

    private final GetMePort getMePort;
    private final ChangeRolePort changeRolePort;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetUserResponseDTO>> getMe(
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID userId
    ) {
        GetUserResponseDTO user = getMePort.execute(userId);
        return ResponseEntity
                .ok(ApiResponse.success(user, "User information retrieved successfully"));
    }

    @PatchMapping("/users/{id}/rol")
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @PathVariable UUID id,
            @RequestBody ChangeRoleRequestDTO request,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID actorUserId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ROLE) UserRole actorRole
    ) {
        changeRolePort.execute(actorUserId, actorRole, id, request.role());
        return ResponseEntity
                .ok(ApiResponse.success(null, "User role updated successfully"));
    }
}