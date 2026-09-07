package com.ticketflow.user.user.application.ports.out;

import com.ticketflow.user.user.domain.models.UserDomain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    Optional<UserDomain> findById(UUID id);

    Optional<UserDomain> findByEmail(String email);

    UserDomain save(UserDomain user);

    boolean existsByEmail(String email);
}