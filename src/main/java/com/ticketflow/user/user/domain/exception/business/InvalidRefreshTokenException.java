package com.ticketflow.user.user.domain.exception.business;

import com.ticketflow.user.common.domain.exception.TicketflowException;

public class InvalidRefreshTokenException extends TicketflowException {

    public InvalidRefreshTokenException() {
        super("The refresh token is invalid or has expired.");
    }
}
