package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class UnauthorizedAdminActionException extends TicketflowException {

    public UnauthorizedAdminActionException() {
        super("You do not have permission to perform this administrative action.");
    }
}
