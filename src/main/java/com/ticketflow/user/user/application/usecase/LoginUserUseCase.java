package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.LoginRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;
import com.ticketflow.user.user.application.ports.in.service.LoginUserPort;
import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.RefreshTokenRepositoryPort;
import com.ticketflow.user.user.application.ports.out.SecureTokenPort;
import com.ticketflow.user.user.application.ports.out.TokenProviderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.AccountSuspendedException;
import com.ticketflow.user.user.domain.exception.business.InvalidCredentialsException;
import com.ticketflow.user.user.domain.models.RefreshTokenDomain;
import com.ticketflow.user.user.domain.models.UserDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase implements LoginUserPort {

    private static final int REFRESH_TOKEN_DAYS = 7;

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final SecureTokenPort secureTokenPort;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        UserDomain user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new AccountSuspendedException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = issueRefreshToken(user.getId());
        return LoginResponseDTO.from(accessToken, refreshToken, user);
    }

    private String issueRefreshToken(UUID userId) {
        String rawToken = secureTokenPort.generate();
        RefreshTokenDomain token = RefreshTokenDomain.create(
                userId,
                secureTokenPort.hash(rawToken),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS)
        );
        refreshTokenRepository.save(token);
        return rawToken;
    }
}
