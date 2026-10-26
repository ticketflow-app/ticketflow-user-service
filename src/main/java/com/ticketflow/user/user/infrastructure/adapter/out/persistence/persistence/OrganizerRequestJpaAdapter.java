package com.ticketflow.user.user.infrastructure.adapter.out.persistence.persistence;

import com.ticketflow.user.user.application.ports.out.OrganizerRequestRepositoryPort;
import com.ticketflow.user.user.domain.models.OrganizerRequestDomain;
import com.ticketflow.user.user.domain.models.enums.OrganizerRequestStatus;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.entity.OrganizerRequestEntity;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.repository.OrganizerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizerRequestJpaAdapter implements OrganizerRequestRepositoryPort {

    private final OrganizerRequestRepository organizerRequestRepository;

    @Override
    public OrganizerRequestDomain save(OrganizerRequestDomain request) {
        OrganizerRequestEntity entity = OrganizerRequestEntity.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .company(request.getCompany())
                .nit(request.getNit())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .resolvedAt(request.getResolvedAt())
                .build();
        return toDomain(organizerRequestRepository.save(entity));
    }

    @Override
    public Optional<OrganizerRequestDomain> findPendingByUserId(UUID userId) {
        return organizerRequestRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, OrganizerRequestStatus.PENDING)
                .map(this::toDomain);
    }

    @Override
    public Optional<OrganizerRequestDomain> findLatestByUserId(UUID userId) {
        return organizerRequestRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(this::toDomain);
    }

    @Override
    public List<OrganizerRequestDomain> findAll() {
        return organizerRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<OrganizerRequestDomain> findById(UUID id) {
        return organizerRequestRepository.findById(id).map(this::toDomain);
    }

    private OrganizerRequestDomain toDomain(OrganizerRequestEntity entity) {
        return OrganizerRequestDomain.from(
                entity.getId(),
                entity.getUserId(),
                entity.getCompany(),
                entity.getNit(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getResolvedAt()
        );
    }
}
