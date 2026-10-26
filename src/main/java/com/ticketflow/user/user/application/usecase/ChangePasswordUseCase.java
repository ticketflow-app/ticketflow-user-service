package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.ChangePasswordRequestDTO;
import com.ticketflow.user.user.application.ports.in.service.ChangePasswordPort;
import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.CurrentPasswordMismatchException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.rules.UserPasswordValidRule;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase implements ChangePasswordPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDTO request) {
        UserDomain user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CurrentPasswordMismatchException();
        }

        UserPasswordValidRule.validate(request.newPassword());
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
