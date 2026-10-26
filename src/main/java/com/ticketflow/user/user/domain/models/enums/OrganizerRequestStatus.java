package com.ticketflow.user.user.domain.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrganizerRequestStatus {

    PENDING("pendiente"),
    APPROVED("aprobada"),
    REJECTED("rechazada");

    private final String value;

    OrganizerRequestStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OrganizerRequestStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (OrganizerRequestStatus status : values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid organizer request status: '" + value + "'");
    }
}
