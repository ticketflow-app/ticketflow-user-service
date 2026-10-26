package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class CurrentPasswordMismatchException extends TicketflowException {

    public CurrentPasswordMismatchException() {
        super("The current password is incorrect.");
    }
}
