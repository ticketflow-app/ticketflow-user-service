package com.ticketflow.user.common.domain.exception;

public class RequiredAttributeException extends TicketflowException {

    public RequiredAttributeException(String attributeName) {
        super(String.format("The field '%s' is required.", attributeName));
    }
}