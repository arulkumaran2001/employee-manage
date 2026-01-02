package com.hrapp.employee_management.serviceImpl;

import com.hrapp.employee_management.exception.InvalidTokenException;
import com.hrapp.employee_management.exception.UserNotFoundException;
import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.repository.UserRepository;
import com.hrapp.employee_management.security.JwtUtils;
import com.hrapp.employee_management.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("🔑 Processing password reset token.");

        if (!jwtUtils.validateToken(token)) {
            log.warn("❌ Invalid or expired password reset token.");
            throw new InvalidTokenException("Invalid or expired token.");
        }

        String email = jwtUtils.getUsernameFromToken(token);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        log.info("✅ Password updated successfully for {}", email);
    }

    @Override
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
        log.debug("🍪 Refresh token cookie set in response.");
    }

    @Override
    public String refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            log.warn("Invalid or expired refresh token detected.");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String email = jwtUtils.getUsernameFromToken(refreshToken);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        List<String> roles = List.of(user.getRole());
        String newAccessToken = jwtUtils.generateAccessToken(email, roles);

        log.info("🔄 New access token generated for {}", email);
        return newAccessToken;
    }

    @Override
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS in production
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.info("Refresh token cookie cleared (logout).");
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }
}
