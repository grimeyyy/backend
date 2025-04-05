package com.grimeyy.backend.auth;

import com.grimeyy.backend.exception.ForbiddenAccessException;
import com.grimeyy.backend.security.JwtUtil;
import com.grimeyy.backend.user.User;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    	Optional<User> userOptional = authService.findUserByEmail(request.getEmail());

        if (userOptional.isEmpty() || !authService.passwordMatches(request.getPassword(), userOptional.get())) {
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
        if (authService.findUserByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User newUser = authService.createNewUser(request.getEmail(), request.getPassword());
        authService.sendVerificationEmail(newUser.getEmail(), newUser.getEmailToken());

        return ResponseEntity.ok("User registered. Please verify your email.");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token);
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmation(@RequestParam("email") String email) {
        User user = authService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailConfirmed()) {
            return ResponseEntity.badRequest().body("Email already confirmed!");
        }

        if (authService.isEmailTokenValid(user)) {
            return ResponseEntity.badRequest().body("Token is still valid!");
        }

        authService.generateEmailToken(user);
        authService.sendVerificationEmail(user.getEmail(), user.getEmailToken());

        return ResponseEntity.ok("New confirmation email sent!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        User user = authService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        authService.generatePasswordResetToken(user);
        authService.sendPasswordResetEmail(user.getEmail(), user.getPasswordResetToken());

        return ResponseEntity.ok("Password reset email sent!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        User user = authService.findUserByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (authService.isResetTokenExpired(user)) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        authService.resetUserPassword(user, newPassword);
        return ResponseEntity.ok("Password successfully reset");
    }
}
