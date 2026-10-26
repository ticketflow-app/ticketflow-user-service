package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class CannotChangeOwnStatusException extends TicketflowException {

    public CannotChangeOwnStatusException() {
        super("An administrator cannot change their own account status.");
    }
}
