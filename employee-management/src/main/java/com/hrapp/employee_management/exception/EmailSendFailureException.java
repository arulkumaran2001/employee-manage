package com.hrapp.employee_management.exception;

public class EmailSendFailureException extends RuntimeException {
    public EmailSendFailureException(String message) {
        super(message);
    }
}
