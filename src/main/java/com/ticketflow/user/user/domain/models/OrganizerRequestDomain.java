package com.ticketflow.user.user.domain.models;

import com.ticketflow.user.user.domain.models.enums.OrganizerRequestStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class OrganizerRequestDomain {

    private final UUID id;
    private final UUID userId;
    private final String company;
    private final String nit;
    private OrganizerRequestStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    private OrganizerRequestDomain(UUID id, UUID userId, String company, String nit,
                                   OrganizerRequestStatus status, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = Objects.requireNonNull(id, "ID is mandatory");
        this.userId = Objects.requireNonNull(userId, "User ID is mandatory");
        this.company = Objects.requireNonNull(company, "Company is mandatory");
        this.nit = Objects.requireNonNull(nit, "NIT is mandatory");
        this.status = Objects.requireNonNull(status, "Status is mandatory");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is mandatory");
        this.resolvedAt = resolvedAt;
    }

    public static OrganizerRequestDomain create(UUID userId, String company, String nit) {
        return new OrganizerRequestDomain(
                UUID.randomUUID(),
                userId,
                company.trim(),
                nit.trim(),
                OrganizerRequestStatus.PENDING,
                LocalDateTime.now(),
                null
        );
    }

    public static OrganizerRequestDomain from(UUID id, UUID userId, String company, String nit,
                                              OrganizerRequestStatus status, LocalDateTime createdAt,
                                              LocalDateTime resolvedAt) {
        return new OrganizerRequestDomain(id, userId, company, nit, status, createdAt, resolvedAt);
    }

    public void approve() {
        this.status = OrganizerRequestStatus.APPROVED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = OrganizerRequestStatus.REJECTED;
        this.resolvedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCompany() {
        return company;
    }

    public String getNit() {
        return nit;
    }

    public OrganizerRequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}
