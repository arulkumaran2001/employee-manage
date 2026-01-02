package com.hrapp.employee_management.serviceImpl;

import com.hrapp.employee_management.exception.EmailSendFailureException;
import com.hrapp.employee_management.exception.UserNotFoundException;
import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.repository.UserRepository;
import com.hrapp.employee_management.security.JwtUtils;
import com.hrapp.employee_management.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public void sendWelcomeEmail(String name, String toEmail, String role, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        String loginUrl = "http://localhost:4200/";
        message.setFrom(adminEmail);
        message.setTo(toEmail);
        message.setSubject("\uD83C\uDF89 Welcome to Java Organization!");
        message.setText(
                "Hello " + name + ",\n\n" +
                        "Congratulations! You have been appointed as a " + role + " at Java Organization.\n" +
                        "Your temporary login password is: " + tempPassword + "\n" +
                        "You can log in using your registered email and this temporary password " + loginUrl + "\n\n" +
                        "Welcome aboard! — we’re excited to have you with us!\n\n" +
                        "Best regards,\n" +
                        "Arul Kumaran\n" +
                        "Admin"
        );

        try {
            mailSender.send(message);
            log.info("🎉 Welcome email sent successfully to '{}'", name);
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to '{}': {}", name, e.getMessage(), e);
            throw new EmailSendFailureException("Failed to send welcome email");
        }
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        log.info("Password reset request for email: {}", email);

        Optional<User> optionalUser = userRepo.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new UserNotFoundException("No user found with this email."));

        String token = jwtUtils.generatePasswordResetToken(user.getEmail());
        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        try {
            sendEmail(email, buildResetEmail(user.getName(), resetLink));
            log.info("📧 Password reset email sent successfully to {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", email, e.getMessage(), e);
            throw new EmailSendFailureException("Failed to send password reset email");
        }
    }

    private void sendEmail(String to, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setTo(to);
        helper.setSubject("🔒 Password Reset Link");
        helper.setText(htmlContent, true);
        helper.setFrom(adminEmail);

        mailSender.send(message);
    }

    private String buildResetEmail(String name, String link) {
        return "<p>Hi " + name + ",</p>" +
                "<p>You requested to reset your password.</p>" +
                "<p>Click the link below to reset it:</p>" +
                "<a href=\"" + link + "\">Reset Password</a>" +
                "<br/><br/><p>This link will expire in 15 minutes. If you didn’t request this, you can ignore this email.</p>";
    }
}
