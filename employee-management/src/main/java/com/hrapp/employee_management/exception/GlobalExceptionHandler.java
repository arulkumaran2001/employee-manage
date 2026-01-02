package com.hrapp.employee_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // -------------------- Existing domain-specific exceptions --------------------

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "User Not Found", request);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidStatusException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "Invalid Status", request);
    }

    @ExceptionHandler(ProfileUploadException.class)
    public ResponseEntity<ErrorResponse> handleProfileUpload(ProfileUploadException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Profile Upload Failed", request);
    }

    @ExceptionHandler(EmailSendFailureException.class)
    public ResponseEntity<ErrorResponse> handleEmailFailure(EmailSendFailureException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Email Sending Failed", request);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.CONFLICT, "Duplicate Email", request);
    }

    // -------------------- Authentication & Authorization exceptions --------------------

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.UNAUTHORIZED, "Invalid Credentials", request);
    }

    // -------------------- AuthService / EmailService RuntimeExceptions --------------------

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "Operation Failed", request);
    }

    // -------------------- Fallback for uncaught exceptions --------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", request);
    }

    // -------------------- Utility --------------------

    private ResponseEntity<ErrorResponse> buildResponse(Exception ex, HttpStatus status, String error, WebRequest request) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                error,
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response, status);
    }
}
