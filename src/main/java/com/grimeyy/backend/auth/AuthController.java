package com.grimeyy.backend.auth;

import com.grimeyy.backend.exception.BadRequestException;
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
        	throw new BadRequestException("ERROR.EMAIL_ALREADY_IN_USE");
        }

        User newUser = authService.createNewUser(request.getEmail(), request.getPassword());
        authService.sendVerificationEmail(newUser.getEmail(), newUser.getEmailToken());

        return ResponseEntity.ok(Map.of("message", "SUCCESS.USER_REGISTERED_VERIFY_YOUR_EMAIL"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendConfirmation(@RequestParam("email") String email) {
        User user = authService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("ERROR.USER_NOT_FOUND"));

        if (user.isEmailConfirmed()) {
        	throw new BadRequestException("ERROR.EMAIL_ALREADY_IN_CONFIRMED");
        }

        if (authService.isEmailTokenValid(user)) {
        	throw new BadRequestException("ERROR.TOKEN_IS_STILL_VALID");
        }

        authService.generateEmailToken(user);
        authService.sendVerificationEmail(user.getEmail(), user.getEmailToken());

        return ResponseEntity.ok(Map.of("message", "SUCCESS.NEW_CONFIRMATION_EMAIL_SENT"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        User user = authService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("ERROR.USER_NOT_FOUND"));

        authService.generatePasswordResetToken(user);
        authService.sendPasswordResetEmail(user.getEmail(), user.getPasswordResetToken());

        return ResponseEntity.ok(Map.of("message", "SUCCESS.PASSWORD_RESET_EMAIL_SENT"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        User user = authService.findUserByResetToken(token)
                .orElseThrow(() -> new RuntimeException("ERROR.INVALID_OR_EXPIRED_TOKEN"));

        if (authService.isResetTokenExpired(user)) {
        	throw new BadRequestException("ERROR.TOKEN_EXPIRED");
        }

        authService.resetUserPassword(user, newPassword);
        return ResponseEntity.ok(Map.of("message", "SUCCESS.PASSWORD_SUCCESSFULLY_RESET"));
    }
}
