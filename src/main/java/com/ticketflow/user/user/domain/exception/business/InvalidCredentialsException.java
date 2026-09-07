package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class InvalidCredentialsException extends TicketflowException {

    public InvalidCredentialsException() {
        super("Invalid credentials.");
    }
}