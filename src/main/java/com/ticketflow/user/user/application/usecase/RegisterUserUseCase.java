package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.RegisterRequestDTO;
import com.ticketflow.user.user.application.ports.in.service.RegisterUserPort;
import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.exception.business.EmailAlreadyExistsException;
import com.ticketflow.user.user.domain.exception.business.RoleNotAllowedForRegistrationException;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.rules.UserEmailValidRule;
import com.ticketflow.user.user.domain.rules.UserNameValidRule;
import com.ticketflow.user.user.domain.rules.UserPasswordValidRule;
import com.ticketflow.user.user.domain.rules.UserRoleValidRule;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase implements RegisterUserPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional
    public GetUserResponseDTO register(RegisterRequestDTO request) {
        UserNameValidRule.validate(request.name());
        UserEmailValidRule.validate(request.email());
        UserPasswordValidRule.validate(request.password());

        UserRole role = request.role() == null ? UserRole.BUYER : request.role();
        UserRoleValidRule.validate(role);
        if (!UserRoleValidRule.isAllowedForRegistration(role)) {
            throw new RoleNotAllowedForRegistrationException();
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        UserDomain user = UserDomain.create(UUID.randomUUID(), request.name(), request.email(), encodedPassword, role);
        userRepository.save(user);

        return GetUserResponseDTO.fromDomain(user);
    }
}