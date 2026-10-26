package com.ticketflow.user.user.domain.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum UserStatus {

    ACTIVE("activo"),
    SUSPENDED("suspendido");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid user status: '" + value + "'");
    }

    public static UserStatus defaultStatus() {
        return ACTIVE;
    }

    public String normalizedValue() {
        return value.toLowerCase(Locale.ROOT);
    }
}
