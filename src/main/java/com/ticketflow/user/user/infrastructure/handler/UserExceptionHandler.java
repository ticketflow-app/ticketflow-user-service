package com.ticketflow.user.user.infrastructure.handler;

import com.ticketflow.user.common.application.dto.ApiResponse;
import com.ticketflow.user.user.domain.exception.business.AccountSuspendedException;
import com.ticketflow.user.user.domain.exception.business.CannotChangeOwnStatusException;
import com.ticketflow.user.user.domain.exception.business.CurrentPasswordMismatchException;
import com.ticketflow.user.user.domain.exception.business.EmailAlreadyExistsException;
import com.ticketflow.user.user.domain.exception.business.InvalidCredentialsException;
import com.ticketflow.user.user.domain.exception.business.InvalidRefreshTokenException;
import com.ticketflow.user.user.domain.exception.business.OrganizerRequestAlreadyExistsException;
import com.ticketflow.user.user.domain.exception.business.OrganizerRequestNotAllowedException;
import com.ticketflow.user.user.domain.exception.business.RoleNotAllowedForRegistrationException;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedRoleChangeException;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.exception.format.UserEmailInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserNameInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserPasswordInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserPhoneInvalidException;
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
        return error(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({UserNameInvalidException.class, UserEmailInvalidException.class,
            UserPasswordInvalidException.class, UserPhoneInvalidException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidUserData(RuntimeException ex) {
        return error(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountSuspended(AccountSuspendedException ex) {
        return error(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(CurrentPasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleCurrentPasswordMismatch(CurrentPasswordMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler({UnauthorizedRoleChangeException.class, UnauthorizedAdminActionException.class,
            OrganizerRequestNotAllowedException.class})
    public ResponseEntity<ApiResponse<Void>> handleForbidden(RuntimeException ex) {
        return error(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler({OrganizerRequestAlreadyExistsException.class, CannotChangeOwnStatusException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(RoleNotAllowedForRegistrationException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotAllowedForRegistration(
            RoleNotAllowedForRegistrationException ex) {
        return error(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex);
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, RuntimeException ex) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage(), null));
    }
}
