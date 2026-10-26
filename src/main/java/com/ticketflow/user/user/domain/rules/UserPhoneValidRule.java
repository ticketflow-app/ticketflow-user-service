package com.ticketflow.user.user.domain.rules;

import com.ticketflow.user.user.domain.exception.format.UserPhoneInvalidException;

public class UserPhoneValidRule {

    private static final String PHONE_REGEX = "^[0-9+()\\-\\s]{7,20}$";

    private UserPhoneValidRule() {}

    public static void validate(String phone) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        if (!phone.trim().matches(PHONE_REGEX)) {
            throw new UserPhoneInvalidException();
        }
    }

    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }
}
