package com.ticketflow.user.user.domain.exception.format;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class UserPasswordInvalidException extends TicketflowException {
    public UserPasswordInvalidException() {
        super("The password is invalid. It must be between 8 and 72 characters long.");
    }
}