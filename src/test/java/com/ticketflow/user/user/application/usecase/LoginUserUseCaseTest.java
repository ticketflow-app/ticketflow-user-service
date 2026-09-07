package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.LoginRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.LoginResponseDTO;
import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.TokenProviderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.exception.business.InvalidCredentialsException;
import com.ticketflow.user.user.domain.models.UserDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginUserUseCaseTest {

    private UserRepositoryPort userRepository;
    private PasswordEncoderPort passwordEncoder;
    private TokenProviderPort tokenProvider;
    private LoginUserUseCase useCase;
    private UserDomain existingUser;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoderPort.class);
        tokenProvider = mock(TokenProviderPort.class);
        useCase = new LoginUserUseCase(userRepository, passwordEncoder, tokenProvider);

        existingUser = UserDomain.from(
                UUID.randomUUID(),
                "Diego",
                "diego@ticketflow.com",
                "$argon2id$encoded",
                UserRole.BUYER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void login_validCredentials_returnsToken() {
        when(userRepository.findByEmail("diego@ticketflow.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password123!", "$argon2id$encoded")).thenReturn(true);
        when(tokenProvider.generateToken(existingUser.getId(), "diego@ticketflow.com", UserRole.BUYER))
                .thenReturn("jwt-token");

        LoginResponseDTO result = useCase.login(new LoginRequestDTO("diego@ticketflow.com", "Password123!"));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("diego@ticketflow.com");
        assertThat(result.role()).isEqualTo(UserRole.BUYER);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        when(userRepository.findByEmail("diego@ticketflow.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> useCase.login(
                new LoginRequestDTO("diego@ticketflow.com", "WrongPassword1!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail("ghost@ticketflow.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.login(
                new LoginRequestDTO("ghost@ticketflow.com", "Password123!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}