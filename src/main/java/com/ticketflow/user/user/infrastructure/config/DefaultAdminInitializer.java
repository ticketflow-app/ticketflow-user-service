package com.ticketflow.user.user.infrastructure.config;

import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.rules.UserEmailValidRule;
import com.ticketflow.user.user.domain.rules.UserNameValidRule;
import com.ticketflow.user.user.domain.rules.UserPasswordValidRule;
import com.ticketflow.user.user.domain.rules.UserPhoneValidRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Creates a default administrator on startup so a fresh database can be used
 * immediately. The operation is idempotent: it never overwrites an existing
 * account and can be disabled with BOOTSTRAP_ADMIN_ENABLED=false.
 */
@Slf4j
@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final boolean enabled;
    private final String name;
    private final String email;
    private final String password;
    private final String phone;

    public DefaultAdminInitializer(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            @Value("${bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${bootstrap.admin.name:Administrador TicketFlow}") String name,
            @Value("${bootstrap.admin.email:admin@ticketflow.com}") String email,
            @Value("${bootstrap.admin.password:Admin123!}") String password,
            @Value("${bootstrap.admin.phone:}") String phone) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Default administrator bootstrap is disabled");
            return;
        }

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank() || password == null || password.isBlank()) {
            log.warn("Default administrator bootstrap skipped: email and password are required");
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.info("Default administrator {} already exists; skipping creation", normalizedEmail);
            return;
        }

        UserNameValidRule.validate(name);
        UserEmailValidRule.validate(normalizedEmail);
        UserPasswordValidRule.validate(password);
        UserPhoneValidRule.validate(phone);

        UserDomain admin = UserDomain.create(
                UUID.randomUUID(),
                name.trim(),
                normalizedEmail,
                UserPhoneValidRule.normalize(phone),
                passwordEncoder.encode(password),
                UserRole.ADMIN);

        userRepository.save(admin);
        log.warn("Created default administrator {}. Change its password after the first login.", normalizedEmail);
    }
}
