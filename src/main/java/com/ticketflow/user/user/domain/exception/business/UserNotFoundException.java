package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class UserNotFoundException extends TicketflowException {

    public UserNotFoundException() {
        super("The user was not found.");
    }
}