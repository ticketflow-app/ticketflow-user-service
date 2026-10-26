package com.ticketflow.user.user.domain.exception.format;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class UserPhoneInvalidException extends TicketflowException {
    public UserPhoneInvalidException() {
        super("The phone number is invalid. It must contain between 7 and 20 digits, spaces, parentheses, hyphens or a leading plus sign.");
    }
}
