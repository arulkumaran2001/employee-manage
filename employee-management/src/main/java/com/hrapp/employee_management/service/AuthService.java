package com.hrapp.employee_management.service;

import com.hrapp.employee_management.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface AuthService {

    Optional<User> findByEmail(String email);

    void resetPassword(String token, String newPassword);

    void addRefreshTokenCookie(HttpServletResponse response, String refreshToken);

    String refreshAccessToken(HttpServletRequest request, HttpServletResponse response);

    void clearRefreshTokenCookie(HttpServletResponse response);

    User getUserByEmail(String email);
}
