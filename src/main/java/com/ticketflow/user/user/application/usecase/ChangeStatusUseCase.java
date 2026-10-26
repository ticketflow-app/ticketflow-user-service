package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.service.ChangeStatusPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.CannotChangeOwnStatusException;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.enums.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeStatusUseCase implements ChangeStatusPort {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Override
    @Transactional
    public void execute(UUID actorUserId, UserRole actorRole, UUID targetUserId, UserStatus newStatus) {
        if (actorRole != UserRole.ADMIN) {
            throw new UnauthorizedAdminActionException();
        }
        if (actorUserId.equals(targetUserId)) {
            throw new CannotChangeOwnStatusException();
        }

        UserDomain user = userRepository.findById(targetUserId)
                .orElseThrow(UserNotFoundException::new);

        user.changeStatus(newStatus);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(targetUserId);
    }
}
