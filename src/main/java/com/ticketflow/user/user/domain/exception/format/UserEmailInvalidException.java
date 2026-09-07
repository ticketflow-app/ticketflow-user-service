package com.ticketflow.user.user.domain.exception.format;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class UserEmailInvalidException extends TicketflowException {
    public UserEmailInvalidException() {
        super("The email is invalid. Provide a valid email address (e.g. user@example.com).");
    }
}