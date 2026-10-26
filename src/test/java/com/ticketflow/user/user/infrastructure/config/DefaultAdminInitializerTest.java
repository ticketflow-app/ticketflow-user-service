package com.ticketflow.user.user.infrastructure.config;

import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.format.UserPasswordInvalidException;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultAdminInitializerTest {

    private UserRepositoryPort userRepository;
    private PasswordEncoderPort passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoderPort.class);
    }

    @Test
    void run_missingAdmin_createsActiveAdminWithEncodedPassword() {
        when(userRepository.existsByEmailIgnoreCase("admin@ticketflow.com")).thenReturn(false);
        when(passwordEncoder.encode("Admin123!")).thenReturn("encoded-password");
        DefaultAdminInitializer initializer = initializer(true, " Administrador TicketFlow ",
                " Admin@TicketFlow.com ", "Admin123!", "  +57 300 123 4567  ");

        initializer.run(null);

        ArgumentCaptor<UserDomain> captor = ArgumentCaptor.forClass(UserDomain.class);
        verify(userRepository).save(captor.capture());
        UserDomain admin = captor.getValue();
        assertThat(admin.getName()).isEqualTo("Administrador TicketFlow");
        assertThat(admin.getEmail()).isEqualTo("admin@ticketflow.com");
        assertThat(admin.getPhone()).isEqualTo("+57 300 123 4567");
        assertThat(admin.getPassword()).isEqualTo("encoded-password");
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void run_existingAdmin_doesNotCreateAnotherUser() {
        when(userRepository.existsByEmailIgnoreCase("admin@ticketflow.com")).thenReturn(true);
        DefaultAdminInitializer initializer = initializer(true, "Administrador TicketFlow",
                "admin@ticketflow.com", "Admin123!", null);

        initializer.run(null);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void run_disabled_doesNothing() {
        DefaultAdminInitializer initializer = initializer(false, "Administrador TicketFlow",
                "admin@ticketflow.com", "Admin123!", null);

        initializer.run(null);

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void run_invalidPassword_throwsPasswordInvalid() {
        when(userRepository.existsByEmailIgnoreCase("admin@ticketflow.com")).thenReturn(false);
        DefaultAdminInitializer initializer = initializer(true, "Administrador TicketFlow",
                "admin@ticketflow.com", "short", null);

        assertThatThrownBy(() -> initializer.run(null))
                .isInstanceOf(UserPasswordInvalidException.class);

        verify(userRepository, never()).save(any());
    }

    private DefaultAdminInitializer initializer(boolean enabled, String name, String email,
                                                String password, String phone) {
        return new DefaultAdminInitializer(
                userRepository, passwordEncoder, enabled, name, email, password, phone);
    }
}
