
package com.grimeyy.backend.auth;


import com.grimeyy.backend.exception.ForbiddenAccessException;
import com.grimeyy.backend.security.JwtUtil;
import com.grimeyy.backend.user.User;
import com.grimeyy.backend.user.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
        	throw new RuntimeException("ERROR.INVALID_CREDENTIALS");
        }
        
        User user = userOptional.get();
        if (!user.isEmailConfirmed()) {
        	throw new ForbiddenAccessException("ERROR.VERIFY_EMAIL_BEFORE_LOGIN");
        }
        
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }
    

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        // Add user
        String token = UUID.randomUUID().toString();
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmailToken(token); // save token
        newUser.setEmailTokenExpiration(Instant.now().plus(15, ChronoUnit.MINUTES)); // token expires after 15 minutes
        newUser.setEmailConfirmed(false); // not confirmed yet
        userRepository.save(newUser);

        // Send email with a confirmation link
        sendVerificationEmail(newUser.getEmail(), token);

        return ResponseEntity.ok("User registered. Please verify your email.");
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        Optional<User> userOptional = userRepository.findByEmailToken(token);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }

        User user = userOptional.get();
        // Check if the token has expired
        if (user.getEmailTokenExpiration().isBefore(Instant.now())) {
        	return ResponseEntity.badRequest().body("Expired token."); // Token expired
        }
        user.setEmailConfirmed(true);
        user.setEmailToken(null); // remove token
        userRepository.save(user);

        return ResponseEntity.ok("Email successfully verified. You can now log in.");
    }
    
    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmation(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailConfirmed()) {
            return ResponseEntity.badRequest().body("Email already confirmed!");
        }

        if (user.getEmailTokenExpiration().isAfter(Instant.now())) {
            return ResponseEntity.badRequest().body("Token is still valid!");
        }

        // generate new token
        String newToken = UUID.randomUUID().toString();
        user.setEmailToken(newToken);
        user.setEmailTokenExpiration(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(user);

        sendVerificationEmail(user.getEmail(), newToken);

        return ResponseEntity.ok("New confirmation email sent!");
    }


    
    private void sendVerificationEmail(String email, String token) {
        String subject = "Confirm your email";
        String confirmationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
        String message = "Click the link to verify your email: " + confirmationUrl;

        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(email);
        emailMessage.setSubject(subject);
        emailMessage.setText(message);
        mailSender.send(emailMessage);
    }
    
}
