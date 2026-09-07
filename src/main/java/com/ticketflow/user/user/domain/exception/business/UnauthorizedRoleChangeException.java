package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class UnauthorizedRoleChangeException extends TicketflowException {

    public UnauthorizedRoleChangeException() {
        super("You do not have permission to change user roles. Only an admin can do this.");
    }
}