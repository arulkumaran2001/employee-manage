package com.hrapp.employee_management.controller;

import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.model.UserStatus;
import com.hrapp.employee_management.repository.AttendanceRepository;
import com.hrapp.employee_management.repository.UserRepository;
import com.hrapp.employee_management.service.EmailService;
import com.hrapp.employee_management.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("salary") Double salary,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setSalary(salary);

        // Generate 8-character temp password
        String tempPassword = generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setStatus(UserStatus.ACTIVE);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/profile/");
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                String safeName = profilePicture.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Files.copy(profilePicture.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                user.setProfilePic("/uploads/profile/" + fileName);
            } catch (IOException e) {
                log.error("❌ Profile picture upload failed:", e);
                return ResponseEntity.status(500).body(Map.of("error", "Profile picture upload failed"));
            }
        }

        userRepository.save(user);

        // Send email using centralized EmailService
        emailService.sendWelcomeEmail(user.getName(),user.getEmail(),user.getRole(), tempPassword);

        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> editUser(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("status") String status,
            @RequestParam("salary") Double salary,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        return userRepository.findById(id).map(user -> {
            user.setName(name);
            user.setEmail(email);
            user.setRole(role);
            user.setSalary(salary);

            try {
                user.setStatus(UserStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
            }

            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/profile/");
                    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                    String originalName = profilePicture.getOriginalFilename();
                    String safeName = originalName != null
                            ? originalName.replaceAll("[^a-zA-Z0-9.-]", "_")
                            : "profile_pic";
                    String fileName = System.currentTimeMillis() + "_" + safeName;

                    Files.copy(profilePicture.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    user.setProfilePic("/uploads/profile/" + fileName);
                } catch (IOException e) {
                    log.error("❌ Profile picture upload failed:", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Profile upload failed"));
                }
            }

            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found")));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(
                    Map.of("message", "User deleted successfully")
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/toggle-status/{userId}")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.DEACTIVATED : UserStatus.ACTIVE);

        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "User status updated",
                "newStatus", user.getStatus()
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/view-users")
    public ResponseEntity<?> viewAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        List<User> users;
        try {
            if (role != null && status != null) {
                users = userRepository.findByRoleAndStatus(role.toUpperCase(), UserStatus.valueOf(status.toUpperCase()));
            } else if (role != null) {
                users = userRepository.findByRole(role.toUpperCase());
            } else if (status != null) {
                users = userRepository.findByStatus(UserStatus.valueOf(status.toUpperCase()));
            } else {
                users = userRepository.findAll();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status or role parameter"));
        }

        return ResponseEntity.ok(users);
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$%&!";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
