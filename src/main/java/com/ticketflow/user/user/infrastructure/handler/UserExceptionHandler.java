package com.ticketflow.user.user.infrastructure.handler;

import com.ticketflow.user.common.application.dto.ApiResponse;
import com.ticketflow.user.user.domain.exception.business.EmailAlreadyExistsException;
import com.ticketflow.user.user.domain.exception.business.InvalidCredentialsException;
import com.ticketflow.user.user.domain.exception.business.RoleNotAllowedForRegistrationException;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedRoleChangeException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.exception.format.UserEmailInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserNameInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserPasswordInvalidException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ticketflow.user.user")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(UserNameInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNameInvalid(UserNameInvalidException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(UserEmailInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserEmailInvalid(UserEmailInvalidException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(UserPasswordInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserPasswordInvalid(UserPasswordInvalidException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(UnauthorizedRoleChangeException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedRoleChange(UnauthorizedRoleChangeException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(RoleNotAllowedForRegistrationException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotAllowedForRegistration(RoleNotAllowedForRegistrationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }
}