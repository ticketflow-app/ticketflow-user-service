package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class RoleNotAllowedForRegistrationException extends TicketflowException {

    public RoleNotAllowedForRegistrationException() {
        super("That role cannot be assigned during registration. Only 'comprador' and 'organizador' are allowed.");
    }
}