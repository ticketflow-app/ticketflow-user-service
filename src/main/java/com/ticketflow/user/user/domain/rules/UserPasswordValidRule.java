package com.ticketflow.user.user.domain.rules;

import com.ticketflow.user.user.domain.exception.format.UserPasswordInvalidException;

public class UserPasswordValidRule {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 72;

    private UserPasswordValidRule() {}

    public static void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new UserPasswordInvalidException();
        }
    }
}