package com.ticketflow.user.user.domain.rules;

import com.ticketflow.user.user.domain.exception.format.UserEmailInvalidException;

public class UserEmailValidRule {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private UserEmailValidRule() {}

    public static void validate(String email) {
        if (email == null || email.isBlank() || email.length() > 255) {
            throw new UserEmailInvalidException();
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new UserEmailInvalidException();
        }
    }
}