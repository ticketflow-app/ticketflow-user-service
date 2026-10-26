package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.RefreshTokenRequestDTO;
import com.ticketflow.user.user.application.ports.in.service.RefreshSessionPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.SecureTokenPort;
import com.ticketflow.user.user.application.ports.out.TokenProviderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.AccountSuspendedException;
import com.ticketflow.user.user.domain.exception.business.InvalidRefreshTokenException;
import com.ticketflow.user.user.domain.models.RefreshTokenDomain;
import com.ticketflow.user.user.domain.models.UserDomain;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshSessionUseCase implements RefreshSessionPort {

    private static final int REFRESH_TOKEN_DAYS = 7;

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final SecureTokenPort secureTokenPort;

    @Override
    @Transactional
    public LoginResponseDTO refresh(RefreshTokenRequestDTO request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash = secureTokenPort.hash(request.refreshToken());
        RefreshTokenDomain stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked()) {
            // A revoked token being reused can mean the token was stolen.
            refreshTokenRepository.revokeAllByUserId(stored.getUserId());
            throw new InvalidRefreshTokenException();
        }

        if (stored.isExpired(LocalDateTime.now())) {
            stored.revoke();
            refreshTokenRepository.save(stored);
            throw new InvalidRefreshTokenException();
        }

        UserDomain user = userRepository.findById(stored.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!user.isActive()) {
            refreshTokenRepository.revokeAllByUserId(user.getId());
            throw new AccountSuspendedException();
        }

        stored.revoke();
        refreshTokenRepository.save(stored);

        String rawRefreshToken = secureTokenPort.generate();
        refreshTokenRepository.save(RefreshTokenDomain.create(
                user.getId(),
                secureTokenPort.hash(rawRefreshToken),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS)
        ));

        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return LoginResponseDTO.from(accessToken, rawRefreshToken, user);
    }
}
