package com.ticketflow.user.user.domain.rules;

import com.ticketflow.user.user.domain.models.enums.UserRole;

public class UserRoleValidRule {

    public static final UserRole[] REGISTRATION_ALLOWED_ROLES = {UserRole.BUYER, UserRole.ORGANIZER};

    private UserRoleValidRule() {}

    public static void validate(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
    }

    public static boolean isAllowedForRegistration(UserRole role) {
        for (UserRole allowed : REGISTRATION_ALLOWED_ROLES) {
            if (allowed == role) {
                return true;
            }
        }
        return false;
    }
}