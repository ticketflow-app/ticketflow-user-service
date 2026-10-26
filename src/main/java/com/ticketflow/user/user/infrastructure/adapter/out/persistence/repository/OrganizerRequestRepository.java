package com.ticketflow.user.user.infrastructure.adapter.out.persistence.repository;

import com.ticketflow.user.user.domain.models.enums.OrganizerRequestStatus;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.entity.OrganizerRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizerRequestRepository extends JpaRepository<OrganizerRequestEntity, UUID> {

    Optional<OrganizerRequestEntity> findFirstByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId, OrganizerRequestStatus status);

    Optional<OrganizerRequestEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    List<OrganizerRequestEntity> findAllByOrderByCreatedAtDesc();
}
