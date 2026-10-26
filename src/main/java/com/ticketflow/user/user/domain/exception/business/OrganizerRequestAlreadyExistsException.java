package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class OrganizerRequestAlreadyExistsException extends TicketflowException {

    public OrganizerRequestAlreadyExistsException() {
        super("You already have a pending organizer request.");
    }
}
