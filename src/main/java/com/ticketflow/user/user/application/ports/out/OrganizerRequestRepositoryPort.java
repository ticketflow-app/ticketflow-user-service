package com.ticketflow.user.user.application.ports.out;

import com.ticketflow.user.user.domain.models.OrganizerRequestDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizerRequestRepositoryPort {

    OrganizerRequestDomain save(OrganizerRequestDomain request);

    Optional<OrganizerRequestDomain> findPendingByUserId(UUID userId);

    Optional<OrganizerRequestDomain> findLatestByUserId(UUID userId);

    List<OrganizerRequestDomain> findAll();

    Optional<OrganizerRequestDomain> findById(UUID id);
}
