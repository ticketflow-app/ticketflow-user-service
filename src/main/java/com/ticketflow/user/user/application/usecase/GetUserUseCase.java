package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.service.GetUserPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserUseCase implements GetUserPort {

    private final UserRepositoryPort userRepository;

    @Override
    public GetUserResponseDTO execute(UUID actorUserId, UserRole actorRole, UUID targetUserId) {
        requireAdmin(actorRole);
        return userRepository.findById(targetUserId)
                .map(GetUserResponseDTO::fromDomain)
                .orElseThrow(UserNotFoundException::new);
    }

    private void requireAdmin(UserRole actorRole) {
        if (actorRole != UserRole.ADMIN) {
            throw new UnauthorizedAdminActionException();
        }
    }
}
