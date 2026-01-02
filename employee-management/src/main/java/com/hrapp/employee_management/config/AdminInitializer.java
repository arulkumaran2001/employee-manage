package com.hrapp.employee_management.config;

import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.model.UserStatus;
import com.hrapp.employee_management.repository.UserRepository;
import com.hrapp.employee_management.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class AdminInitializer {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.admin.email}")          // required — must be defined in application.properties
    private String adminEmail;

    @Value("${app.admin.password:}")      // optional — empty means generate random temp password
    private String adminPassword;

    @Value("${app.admin.name:Admin}")     // optional — defaults to "Admin"
    private String adminName;

    public AdminInitializer(UserRepository userRepository,
                            BCryptPasswordEncoder passwordEncoder,
                            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminIfNotExists() {
        // Check email configuration
        if (adminEmail == null || adminEmail.isBlank()) {
            log.error("❌ ERROR: app.admin.email is missing in application.properties.");
            log.error("➡️ Please provide a valid admin email to bootstrap the system.");
            throw new IllegalStateException("Missing admin email configuration");
        }

        // Check if admin already exists
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("✅ Admin already exists: {}", adminEmail);
            return;
        }

        // Generate password if not provided
        String finalPassword = (adminPassword == null || adminPassword.isBlank())
                ? generateRandomPassword(8)
                : adminPassword;

        // Create admin user
        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(finalPassword));
        admin.setRole("ADMIN");
        admin.setStatus(UserStatus.ACTIVE);
        admin.setSalary(0.0); // keep consistent with DB schema (not null column)

        userRepository.save(admin);

        // Log info safely
        log.info("⚙️ Admin account created automatically!");
        log.info("📧 Email: {}", adminEmail);

        // Only log temp password in non-production environments
        String env = System.getProperty("spring.profiles.active", "default");
        if (!"prod".equalsIgnoreCase(env)) {
            log.info("🔑 Temporary Password: {}", finalPassword);
        } else {
            log.info("🔑 Temporary Password is set. Check secure channel for first login.");
        }

        log.info("➡️ Please login and reset your password immediately.");

        // Send email using centralized EmailService
        emailService.sendWelcomeEmail(adminName,adminEmail,admin.getRole(), finalPassword);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$%&!";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
