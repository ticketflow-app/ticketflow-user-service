package com.ticketflow.user.user.infrastructure.adapter.in.controller;

import com.ticketflow.user.common.application.dto.ApiResponse;
import com.ticketflow.user.common.application.dto.PageResponse;
import com.ticketflow.user.user.application.ports.in.dto.ChangePasswordRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.ChangeRoleRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.ChangeStatusRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.UpdateProfileRequestDTO;
import com.ticketflow.user.user.application.ports.in.service.ChangePasswordPort;
import com.ticketflow.user.user.application.ports.in.service.ChangeRolePort;
import com.ticketflow.user.user.application.ports.in.service.ChangeStatusPort;
import com.ticketflow.user.user.application.ports.in.service.GetMePort;
import com.ticketflow.user.user.application.ports.in.service.GetUserPort;
import com.ticketflow.user.user.application.ports.in.service.ListOrganizerRequestsPort;
import com.ticketflow.user.user.application.ports.in.service.ListUsersPort;
import com.ticketflow.user.user.application.ports.in.service.RequestOrganizerPort;
import com.ticketflow.user.user.application.ports.in.service.UpdateProfilePort;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.infrastructure.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for authenticated user information and administration")
public class UserController {

    private final GetMePort getMePort;
    private final UpdateProfilePort updateProfilePort;
    private final ChangePasswordPort changePasswordPort;
    private final RequestOrganizerPort requestOrganizerPort;
    private final ChangeRolePort changeRolePort;
    private final ChangeStatusPort changeStatusPort;
    private final GetUserPort getUserPort;
    private final ListUsersPort listUsersPort;
    private final ListOrganizerRequestsPort listOrganizerRequestsPort;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetUserResponseDTO>> getMe(
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID userId
    ) {
        GetUserResponseDTO user = getMePort.execute(userId);
        return ResponseEntity
                .ok(ApiResponse.success(user, "User information retrieved successfully"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<GetUserResponseDTO>> updateProfile(
            @RequestBody UpdateProfileRequestDTO request,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID userId
    ) {
        GetUserResponseDTO user = updateProfilePort.update(userId, request);
        return ResponseEntity
                .ok(ApiResponse.success(user, "Profile updated successfully"));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequestDTO request,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID userId
    ) {
        changePasswordPort.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/organizador/solicitud")
    public ResponseEntity<ApiResponse<OrganizerRequestResponseDTO>> requestOrganizer(
            @RequestBody OrganizerRequestDTO request,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID userId
    ) {
        OrganizerRequestResponseDTO response = requestOrganizerPort.request(userId, request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Organizer request submitted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GetUserResponseDTO>>> listUsers(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID actorUserId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ROLE) UserRole actorRole
    ) {
        PageResponse<GetUserResponseDTO> users = listUsersPort.execute(actorUserId, actorRole, q, page, size);
        return ResponseEntity
                .ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/organizador/solicitudes")
    public ResponseEntity<ApiResponse<List<OrganizerRequestResponseDTO>>> listOrganizerRequests(
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID actorUserId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ROLE) UserRole actorRole
    ) {
        List<OrganizerRequestResponseDTO> requests = listOrganizerRequestsPort.execute(actorUserId, actorRole);
        return ResponseEntity
                .ok(ApiResponse.success(requests, "Organizer requests retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetUserResponseDTO>> getUser(
            @PathVariable UUID id,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID actorUserId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ROLE) UserRole actorRole
    ) {
        GetUserResponseDTO user = getUserPort.execute(actorUserId, actorRole, id);
        return ResponseEntity
                .ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @PatchMapping("/{id}/rol")
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

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable UUID id,
            @RequestBody ChangeStatusRequestDTO request,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID actorUserId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ROLE) UserRole actorRole
    ) {
        changeStatusPort.execute(actorUserId, actorRole, id, request.status());
        return ResponseEntity
                .ok(ApiResponse.success(null, "User status updated successfully"));
    }
}
