package com.ticketflow.user.user.application.ports.out;

import com.ticketflow.user.user.domain.models.UserDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    Optional<UserDomain> findById(UUID id);

    Optional<UserDomain> findByEmail(String email);

    UserDomain save(UserDomain user);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UserDomain> search(String query, int page, int size);

    long count(String query);
}
