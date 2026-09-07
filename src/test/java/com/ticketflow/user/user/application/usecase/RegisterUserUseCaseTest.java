package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.RegisterRequestDTO;
import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.exception.business.EmailAlreadyExistsException;
import com.ticketflow.user.user.domain.exception.business.RoleNotAllowedForRegistrationException;
import com.ticketflow.user.user.domain.exception.format.UserNameInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserPasswordInvalidException;
import com.ticketflow.user.user.domain.models.UserDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterUserUseCaseTest {

    private UserRepositoryPort userRepository;
    private PasswordEncoderPort passwordEncoder;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoderPort.class);
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    void register_validRequest_createsUser() {
        when(userRepository.existsByEmail("diego@ticketflow.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$argon2id$encoded");
        when(userRepository.save(any(UserDomain.class))).thenAnswer(inv -> inv.getArgument(0));

        GetUserResponseDTO result = useCase.register(
                new RegisterRequestDTO("Diego", "diego@ticketflow.com", "Password123!", null));

        assertThat(result.email()).isEqualTo("diego@ticketflow.com");
        assertThat(result.role()).isEqualTo(UserRole.BUYER);
        assertThat(result.name()).isEqualTo("Diego");
    }

    @Test
    void register_existingEmail_throwsEmailAlreadyExists() {
        when(userRepository.existsByEmail("diego@ticketflow.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.register(
                new RegisterRequestDTO("Diego", "diego@ticketflow.com", "Password123!", null)))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void register_staffRole_throwsRoleNotAllowed() {
        assertThatThrownBy(() -> useCase.register(
                new RegisterRequestDTO("Diego", "diego@ticketflow.com", "Password123!", UserRole.STAFF_CHECKIN)))
                .isInstanceOf(RoleNotAllowedForRegistrationException.class);
    }

    @Test
    void register_invalidName_throwsUserNameInvalid() {
        assertThatThrownBy(() -> useCase.register(
                new RegisterRequestDTO("x", "diego@ticketflow.com", "Password123!", null)))
                .isInstanceOf(UserNameInvalidException.class);
    }

    @Test
    void register_invalidPassword_throwsUserPasswordInvalid() {
        assertThatThrownBy(() -> useCase.register(
                new RegisterRequestDTO("Diego", "diego@ticketflow.com", "123", null)))
                .isInstanceOf(UserPasswordInvalidException.class);
    }
}