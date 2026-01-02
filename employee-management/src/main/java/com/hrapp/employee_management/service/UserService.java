package com.hrapp.employee_management.service;

import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.model.UserStatus;
import com.hrapp.employee_management.repository.AttendanceRepository;
import com.hrapp.employee_management.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       AttendanceRepository attendanceRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------- EXISTING METHODS (UNCHANGED) ----------------

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public User loginUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new RuntimeException("Your account is deactivated. Please contact admin.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void updateStatus(Long userId, UserStatus status) {
        User user = findById(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<User> findAllEmployeesSorted() {
        return userRepository.findByRoleOrderByStatusAscNameAsc("EMPLOYEE");
    }

    // ---------------- DELETE LOGIC (NEW & PROTECTED) ----------------

    @Transactional
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🚫 BLOCK ADMIN DELETION
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalStateException("Admin user cannot be deleted");
        }

        // ✅ Delete attendance first
        attendanceRepository.deleteByEmployeeId(userId);

        // ✅ Delete user
        userRepository.delete(user);
    }
}
