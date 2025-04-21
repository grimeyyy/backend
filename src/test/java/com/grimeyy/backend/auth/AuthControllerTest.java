package com.grimeyy.backend.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimeyy.backend.user.User;
import jakarta.servlet.http.Cookie;

import com.grimeyy.backend.auth.dto.LoginRequest;
import com.grimeyy.backend.exception.BadRequestException;
import com.grimeyy.backend.security.jwt.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "test@example.com";
    private final String password = "secret123";
    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setEmailConfirmed(true);
    }

    @Test
    void login_success() throws Exception {
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(authService.passwordMatches(password, user)).thenReturn(true);
        when(jwtUtil.generateAccessToken(email)).thenReturn("accessToken123");
        when(jwtUtil.generateRefreshToken(email)).thenReturn("refreshToken123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("accessToken123"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void login_shouldFail_whenInvalidPassword() throws Exception {
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(authService.passwordMatches(password, user)).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
        		.andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldFail_whenEmailNotVerified() throws Exception {
        user.setEmailConfirmed(false);
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(authService.passwordMatches(password, user)).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void login_shouldFail_whenUserNotFound() throws Exception {
        when(authService.findUserByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("ERROR.INVALID_CREDENTIALS"));
    }


    @Test
    void refreshToken_success() throws Exception {
        String refreshToken = "refreshToken123";
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiration(Instant.now().plus(1, ChronoUnit.DAYS));

        when(jwtUtil.extractEmail(refreshToken)).thenReturn(email);
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(authService.isRefreshTokenValid(user, refreshToken)).thenReturn(true);
        when(jwtUtil.generateAccessToken(email)).thenReturn("newAccessToken");

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("newAccessToken"));
    }
    
    @Test
    void refreshToken_shouldFail_whenNoTokenProvided() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ERROR.MISSING_REFRESH_TOKEN"));
    }

    @Test
    void refreshToken_shouldFail_whenTokenInvalidOrExpired() throws Exception {
        String refreshToken = "expiredToken";
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiration(Instant.now().minus(1, ChronoUnit.HOURS));

        when(jwtUtil.extractEmail(refreshToken)).thenReturn(email);
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(authService.isRefreshTokenValid(user, refreshToken)).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ERROR.INVALID_OR_EXPIRED_REFRESH_TOKEN"));
    }


    @Test
    void register_success() throws Exception {
        when(authService.findUserByEmail(email)).thenReturn(Optional.empty());
        when(authService.createNewUser(eq(email), anyString())).thenReturn(user);

        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS.USER_REGISTERED_VERIFY_YOUR_EMAIL"));
    }
    
    @Test
    void register_shouldFail_whenEmailAlreadyExists() throws Exception {
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ERROR.EMAIL_ALREADY_IN_USE"));
    }

    @Test
    void verifyEmail_success() throws Exception {
        String token = "token123";

        ResponseEntity<Map<String, String>> response = ResponseEntity.ok(Map.of("message", "SUCCESS.EMAIL_SUCCESSFULLY_VERIFIED"));
        when(authService.verifyEmail(eq(token)))
                .thenReturn(response); 

        mockMvc.perform(get("/api/auth/verify-email")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS.EMAIL_SUCCESSFULLY_VERIFIED"));
    }
    
    @Test
    void verifyEmail_shouldFail_whenTokenInvalid() throws Exception {
        String token = "invalidToken";

        when(authService.verifyEmail(eq(token)))
                .thenThrow(new BadRequestException("ERROR.INVALID_TOKEN"));

        mockMvc.perform(get("/api/auth/verify-email")
                .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ERROR.INVALID_TOKEN"));
    }


    @Test
    void resendVerification_success() throws Exception {
        user.setEmailConfirmed(false);
        user.setEmailTokenExpiration(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(authService.isEmailTokenValid(user)).thenReturn(false);

        mockMvc.perform(post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS.NEW_CONFIRMATION_EMAIL_SENT"));
    }
    
    @Test
    void resendVerification_shouldFail_whenEmailAlreadyConfirmed() throws Exception {
        user.setEmailConfirmed(true);

        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ERROR.EMAIL_ALREADY_CONFIRMED"));
    }


    @Test
    void forgotPassword_success() throws Exception {
        when(authService.findUserByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS.PASSWORD_RESET_EMAIL_SENT"));
    }

    @Test
    void resetPassword_success() throws Exception {
        String token = "reset-token";
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiration(Instant.now().plus(5, ChronoUnit.MINUTES));

        when(authService.findUserByResetToken(token)).thenReturn(Optional.of(user));
        when(authService.isResetTokenExpired(user)).thenReturn(false);

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "token", token,
                        "newPassword", "newPassword123"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS.PASSWORD_SUCCESSFULLY_RESET"));
    }
    
    @Test
    void resetPassword_shouldFail_whenTokenInvalid() throws Exception {
        String token = "invalidToken";
        when(authService.findUserByResetToken(token)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "token", token,
                        "newPassword", "newPassword123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ERROR.INVALID_OR_EXPIRED_TOKEN"));
    }

    @Test
    void resetPassword_shouldFail_whenTokenExpired() throws Exception {
        String token = "expiredToken";
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiration(Instant.now().minus(5, ChronoUnit.MINUTES));

        when(authService.findUserByResetToken(token)).thenReturn(Optional.of(user));
        when(authService.isResetTokenExpired(user)).thenReturn(true);

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "token", token,
                        "newPassword", "newPassword123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ERROR.TOKEN_EXPIRED"));
    }


    @Test
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS.LOGGED_OUT"))
                .andExpect(header().exists("Set-Cookie"));
    }
}

