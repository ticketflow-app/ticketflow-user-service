package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class AccountSuspendedException extends TicketflowException {

    public AccountSuspendedException() {
        super("This account is suspended. Contact an administrator.");
    }
}
