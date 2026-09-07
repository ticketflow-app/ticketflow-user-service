package com.ticketflow.user.user.infrastructure.adapter.out.security;

import com.ticketflow.user.user.application.ports.out.PasswordEncoderPort;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class Argon2PasswordEncoder implements PasswordEncoderPort {

    private static final int ITERATIONS = 2;
    private static final int MEMORY_IN_KIB = 65536;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    @Override
    public String encode(String rawPassword) {
        return argon2.hash(ITERATIONS, MEMORY_IN_KIB, PARALLELISM,
                rawPassword.toCharArray(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return argon2.verify(encodedPassword, rawPassword.toCharArray(), StandardCharsets.UTF_8);
    }
}