package com.ticketflow.user.user.domain.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {

    BUYER("comprador"),
    ORGANIZER("organizador"),
    STAFF_CHECKIN("staff_checkin"),
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserRole from(String value) {
        if (value == null) {
            return null;
        }
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: '" + value + "'");
    }
}