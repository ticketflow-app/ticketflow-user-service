package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class EmailAlreadyExistsException extends TicketflowException {

    public EmailAlreadyExistsException() {
        super("There is already an account registered with that email.");
    }
}