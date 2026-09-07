package com.ticketflow.user.user.domain.rules;

import com.ticketflow.user.common.domain.exception.RequiredAttributeException;
import com.ticketflow.user.common.domain.rules.DomainRule;
import com.ticketflow.user.user.domain.models.UserDomain;

public class UserValidatorRule implements DomainRule<UserDomain> {

    @Override
    public UserDomain validate(UserDomain user) {
        if (user == null) {
            throw new RequiredAttributeException("user");
        }
        if (user.getId() == null) {
            throw new RequiredAttributeException("id");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new RequiredAttributeException("name");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RequiredAttributeException("email");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RequiredAttributeException("password");
        }
        if (user.getRole() == null) {
            throw new RequiredAttributeException("rol");
        }
        if (user.getCreatedAt() == null) {
            throw new RequiredAttributeException("createdAt");
        }
        if (user.getUpdatedAt() == null) {
            throw new RequiredAttributeException("updatedAt");
        }
        return user;
    }
}