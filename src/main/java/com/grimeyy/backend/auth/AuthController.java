package com.grimeyy.backend.auth;

import com.grimeyy.backend.auth.dto.LoginRequest;
import com.grimeyy.backend.exception.BadRequestException;
import com.grimeyy.backend.exception.ForbiddenAccessException;
import com.grimeyy.backend.exception.UnauthorizedException;
import com.grimeyy.backend.security.jwt.JwtUtil;
import com.grimeyy.backend.user.User;

import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        Optional<User> userOptional = authService.findUserByEmail(request.getEmail());

        if (userOptional.isEmpty() || !authService.passwordMatches(request.getPassword(), userOptional.get())) {
            throw new UnauthorizedException("ERROR.INVALID_CREDENTIALS");
        }

        User user = userOptional.get();
        if (!user.isEmailConfirmed()) {
            throw new ForbiddenAccessException("ERROR.VERIFY_EMAIL_BEFORE_LOGIN");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        authService.saveRefreshToken(user, refreshToken);

        // set cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // only over HTTPS in production
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(Map.of("token", accessToken));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new BadRequestException("ERROR.MISSING_REFRESH_TOKEN");
        }

        String email;
        try {
            email = jwtUtil.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new BadRequestException("ERROR.INVALID_REFRESH_TOKEN");
        }

        User user = authService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("ERROR.USER_NOT_FOUND"));

        if (!authService.isRefreshTokenValid(user, refreshToken)) {
            throw new BadRequestException("ERROR.INVALID_OR_EXPIRED_REFRESH_TOKEN");
        }

        String newAccessToken = jwtUtil.generateAccessToken(email);
        return ResponseEntity.ok(Map.of("token", newAccessToken));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());
        return ResponseEntity.ok(Map.of("message", "SUCCESS.LOGGED_OUT"));
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
    public ResponseEntity<?> resendConfirmation(@RequestBody Map<String, String> request) {
    	String email = request.get("email");
        User user = authService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("ERROR.USER_NOT_FOUND"));

        if (user.isEmailConfirmed()) {
        	throw new BadRequestException("ERROR.EMAIL_ALREADY_CONFIRMED");
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
                .orElseThrow(() -> new BadRequestException("ERROR.INVALID_OR_EXPIRED_TOKEN"));

        if (authService.isResetTokenExpired(user)) {
        	throw new BadRequestException("ERROR.TOKEN_EXPIRED");
        }

        authService.resetUserPassword(user, newPassword);
        return ResponseEntity.ok(Map.of("message", "SUCCESS.PASSWORD_SUCCESSFULLY_RESET"));
    }
    
}
