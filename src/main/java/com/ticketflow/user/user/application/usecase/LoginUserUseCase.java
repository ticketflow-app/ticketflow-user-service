package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.service.LoginUserPort;
import com.ticketflow.user.user.application.ports.in.dto.LoginRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;
import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.TokenProviderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.InvalidCredentialsException;
import com.ticketflow.user.user.domain.models.UserDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase implements LoginUserPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        UserDomain user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return LoginResponseDTO.from(token, user);
    }
}