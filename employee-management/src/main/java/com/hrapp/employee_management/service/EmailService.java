package com.hrapp.employee_management.service;

public interface EmailService {
    void sendWelcomeEmail(String name, String toEmail, String role, String tempPassword);
    void sendPasswordResetEmail(String email);
}
