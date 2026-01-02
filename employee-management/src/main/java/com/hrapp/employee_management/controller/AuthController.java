package com.hrapp.employee_management.controller;

import com.hrapp.employee_management.exception.InvalidTokenException;
import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.security.CustomUserDetails;
import com.hrapp.employee_management.security.JwtUtils;
import com.hrapp.employee_management.service.AuthService;
import com.hrapp.employee_management.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Restrict in production
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Send password reset email */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        emailService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "If the email exists, a reset link has been sent."
        ));
    }

    /** Reset password using token */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        log.info("Password reset successful for token {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        return ResponseEntity.ok(Map.of("message", "Password updated successfully!"));
    }

    /** Login and generate access + refresh tokens */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        log.info("Login attempt for email: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<String> roles = List.of(user.getRole());

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        authService.addRefreshTokenCookie(response, refreshToken);

        log.info("✅ Login successful for email: {} | Role: {}", user.getEmail(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(accessToken, roles.get(0), user.getEmail(), user.getName(), "Login successful"));
    }

    // DTOs
    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class AuthResponse {
        private final String token;
        private final String role;
        private final String email;
        private final String name;
        private final String statusMessage;

        public AuthResponse(String token, String role, String email, String name, String statusMessage) {
            this.token = token;
            this.role = role;
            this.email = email;
            this.name = name;
            this.statusMessage = statusMessage;
        }

        public String getToken() { return token; }
        public String getRole() { return role; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getStatusMessage() { return statusMessage; }
    }
}
