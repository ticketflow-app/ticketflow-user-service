package com.ticketflow.user.user.domain.exception.format;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class UserNameInvalidException extends TicketflowException {
    public UserNameInvalidException() {
        super("The user name is invalid. It must be between 3 and 50 characters long and cannot contain special characters.");
    }
}