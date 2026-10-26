package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.service.ChangeRolePort;
import com.ticketflow.user.user.application.ports.out.OrganizerRequestRepositoryPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedRoleChangeException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeRoleUseCase implements ChangeRolePort {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final OrganizerRequestRepositoryPort organizerRequestRepository;

    @Override
    @Transactional
    public void execute(UUID actorUserId, UserRole actorRole, UUID targetUserId, UserRole newRole) {
        if (actorRole != UserRole.ADMIN) {
            throw new UnauthorizedRoleChangeException();
        }

        UserDomain user = userRepository.findById(targetUserId)
                .orElseThrow(UserNotFoundException::new);

        user.changeRole(newRole);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(targetUserId);

        if (newRole == UserRole.ORGANIZER) {
            organizerRequestRepository.findPendingByUserId(targetUserId).ifPresent(request -> {
                request.approve();
                organizerRequestRepository.save(request);
            });
        }
    }
}
