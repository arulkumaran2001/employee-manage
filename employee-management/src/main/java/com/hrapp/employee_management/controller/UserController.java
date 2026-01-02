package com.hrapp.employee_management.controller;

import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.model.UserStatus;
import com.hrapp.employee_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User user = userService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePic()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/{id}/salary")
    public ResponseEntity<?> updateSalary(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        if (body.get("salary") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing salary in request body"));
        }

        double newSalary;
        try {
            newSalary = Double.parseDouble(body.get("salary").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid salary format"));
        }

        user.setSalary(newSalary);
        userService.save(user);

        return ResponseEntity.ok(Map.of("message", "Salary updated successfully", "user", user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/profile-pic")
    public ResponseEntity<?> uploadProfilePicForUser(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            Path uploadDir = Paths.get("uploads/profile/");
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            User user = userService.findById(id);
            if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));

            user.setProfilePic("/uploads/profile/" + filename);
            userService.save(user);

            return ResponseEntity.ok(Map.of("profilePic", user.getProfilePic()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to upload file"));
        }
    }

    static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String profilePic;

        public UserResponse(Long id, String name, String email, String role, String profilePic) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.profilePic = profilePic;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getProfilePic() { return profilePic; }
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/hr/employees")
    public ResponseEntity<?> getAllEmployeesForHR() {
        List<User> users = userService.findAllEmployeesSorted();
        List<EmployeeHRResponse> employees = users.stream()
                .map(u -> new EmployeeHRResponse(u.getId(), u.getName(), u.getEmail(), u.getStatus(), u.getSalary()))
                .toList();

        return ResponseEntity.ok(employees);
    }

    static class EmployeeHRResponse {
        private Long id;
        private String name;
        private String email;
        private UserStatus status;
        private Double salary;

        public EmployeeHRResponse(Long id, String name, String email, UserStatus status, Double salary) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.status = status;
            this.salary = salary;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public UserStatus getStatus() { return status; }
        public Double getSalary() { return salary; }
    }
}
