package com.ticketflow.user.common.domain.rules;

public interface DomainRule<T> {
    T validate(T data);
}