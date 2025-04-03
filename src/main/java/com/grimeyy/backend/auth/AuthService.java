package com.grimeyy.backend.auth;

import com.grimeyy.backend.user.User;
import com.grimeyy.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserByResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    public boolean passwordMatches(String rawPassword, User user) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public User createNewUser(String email, String password) {
        String token = UUID.randomUUID().toString();
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmailToken(token);
        newUser.setEmailTokenExpiration(Instant.now().plus(15, ChronoUnit.MINUTES));
        newUser.setEmailConfirmed(false);
        return userRepository.save(newUser);
    }

    public void sendVerificationEmail(String email, String token) {
        sendEmail(email, "Confirm your email",
                "Click the link to verify your email: " + baseUrl + "/api/auth/verify-email?token=" + token);
    }

    public void sendPasswordResetEmail(String email, String token) {
        sendEmail(email, "Reset your password",
                "Click the link to reset your password: " + frontendUrl + "/reset-password?token=" + token);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(to);
        emailMessage.setSubject(subject);
        emailMessage.setText(text);
        mailSender.send(emailMessage);
    }

    public void generateEmailToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setEmailToken(token);
        user.setEmailTokenExpiration(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(user);
    }

    public void generatePasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiration(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(user);
    }

    public boolean isEmailTokenValid(User user) {
        return user.getEmailTokenExpiration().isAfter(Instant.now());
    }

    public boolean isResetTokenExpired(User user) {
        return user.getPasswordResetTokenExpiration().isBefore(Instant.now());
    }

    public ResponseEntity<?> verifyEmail(String token) {
        Optional<User> userOptional = userRepository.findByEmailToken(token);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }

        User user = userOptional.get();
        if (user.getEmailTokenExpiration().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body("Expired token.");
        }

        user.setEmailConfirmed(true);
        user.setEmailToken(null);
        userRepository.save(user);

        return ResponseEntity.ok("Email successfully verified. You can now log in.");
    }

    public void resetUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiration(null);
        userRepository.save(user);
    }
}
