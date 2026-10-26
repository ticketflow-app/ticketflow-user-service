package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class OrganizerRequestNotAllowedException extends TicketflowException {

    public OrganizerRequestNotAllowedException() {
        super("Only buyers can request an organizer account.");
    }
}
