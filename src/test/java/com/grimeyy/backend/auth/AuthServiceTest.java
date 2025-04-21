package com.grimeyy.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;


import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.grimeyy.backend.exception.BadRequestException;
import com.grimeyy.backend.user.User;
import com.grimeyy.backend.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private AuthService authService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("securePass123"));
        return user;
    }

    @Test
    void findUserByEmail_shouldReturnUser() {
        String email = "test@example.com";
        User user = createUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = authService.findUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    void passwordMatches_shouldReturnTrue_whenPasswordIsCorrect() {
        User user = createUser();
        boolean matches = authService.passwordMatches("securePass123", user);
        assertTrue(matches);
    }

    @Test
    void passwordMatches_shouldReturnFalse_whenPasswordIsIncorrect() {
        User user = createUser();
        boolean matches = authService.passwordMatches("wrongPass", user);
        assertFalse(matches);
    }

    @Test
    void createNewUser_shouldCreateAndSaveUser() {
        String email = "new@example.com";
        String password = "myPassword123";

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.createNewUser(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertNotNull(result.getEmailToken());
        assertFalse(result.isEmailConfirmed());
        assertTrue(passwordEncoder.matches(password, result.getPassword()));

        verify(userRepository).save(captor.capture());
        assertEquals(email, captor.getValue().getEmail());
    }

    @Test
    void sendVerificationEmail_shouldSendEmail() {
        String email = "test@example.com";
        String token = "test-token";

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        authService.sendVerificationEmail(email, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertEquals(email, msg.getTo()[0]);
        assertTrue(msg.getText().contains(frontendUrl));
        assertTrue(msg.getText().contains(token));
    }

    @Test
    void isEmailTokenValid_shouldReturnTrue_whenNotExpired() {
        User user = createUser();
        user.setEmailTokenExpiration(Instant.now().plusSeconds(60));
        assertTrue(authService.isEmailTokenValid(user));
    }

    @Test
    void isEmailTokenValid_shouldReturnFalse_whenExpired() {
        User user = createUser();
        user.setEmailTokenExpiration(Instant.now().minusSeconds(60));
        assertFalse(authService.isEmailTokenValid(user));
    }

    @Test
    void isResetTokenExpired_shouldReturnTrue_whenExpired() {
        User user = createUser();
        user.setPasswordResetTokenExpiration(Instant.now().minusSeconds(1));
        assertTrue(authService.isResetTokenExpired(user));
    }

    @Test
    void isResetTokenExpired_shouldReturnFalse_whenNotExpired() {
        User user = createUser();
        user.setPasswordResetTokenExpiration(Instant.now().plusSeconds(60));
        assertFalse(authService.isResetTokenExpired(user));
    }

    @Test
    void saveRefreshToken_shouldSetTokenAndExpiration() {
        User user = createUser();
        String token = "refresh-token";

        authService.saveRefreshToken(user, token);

        assertEquals(token, user.getRefreshToken());
        assertNotNull(user.getRefreshTokenExpiration());
        assertTrue(user.getRefreshTokenExpiration().isAfter(Instant.now()));
        verify(userRepository).save(user);
    }

    @Test
    void isRefreshTokenValid_shouldReturnTrue_whenValid() {
        User user = createUser();
        String token = "refresh-token";
        user.setRefreshToken(token);
        user.setRefreshTokenExpiration(Instant.now().plusSeconds(60));

        assertTrue(authService.isRefreshTokenValid(user, token));
    }

    @Test
    void isRefreshTokenValid_shouldReturnFalse_whenTokenInvalid() {
        User user = createUser();
        user.setRefreshToken("other-token");
        user.setRefreshTokenExpiration(Instant.now().plusSeconds(60));

        assertFalse(authService.isRefreshTokenValid(user, "refresh-token"));
    }

    @Test
    void isRefreshTokenValid_shouldReturnFalse_whenExpired() {
        User user = createUser();
        String token = "refresh-token";
        user.setRefreshToken(token);
        user.setRefreshTokenExpiration(Instant.now().minusSeconds(60));

        assertFalse(authService.isRefreshTokenValid(user, token));
    }
    
    @Test
    void verifyEmail_shouldVerifySuccessfully_whenTokenIsValid() {
        String token = "valid-token";
        User user = createUser();
        user.setEmailToken(token);
        user.setEmailTokenExpiration(Instant.now().plusSeconds(300));

        when(userRepository.findByEmailToken(token)).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, String>> response = authService.verifyEmail(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("SUCCESS.EMAIL_SUCCESSFULLY_VERIFIED"));
        assertTrue(user.isEmailConfirmed());
        assertNull(user.getEmailToken());

        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_shouldThrow_whenTokenIsInvalid() {
        String token = "invalid-token";
        when(userRepository.findByEmailToken(token)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.verifyEmail(token)
        );

        assertEquals("ERROR.INVALID_OR_EXPIRED_TOKEN", exception.getMessage());
    }

    @Test
    void verifyEmail_shouldThrow_whenTokenExpired() {
        String token = "expired-token";
        User user = createUser();
        user.setEmailToken(token);
        user.setEmailTokenExpiration(Instant.now().minusSeconds(60));

        when(userRepository.findByEmailToken(token)).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.verifyEmail(token)
        );

        assertEquals("ERROR.TOKEN_EXPIRED", exception.getMessage());
    }
    
    @Test
    void generateEmailToken_shouldGenerateAndSaveToken() {
        User user = createUser();
        assertNull(user.getEmailToken());

        authService.generateEmailToken(user);

        assertNotNull(user.getEmailToken());
        assertNotNull(user.getEmailTokenExpiration());
        assertTrue(user.getEmailTokenExpiration().isAfter(Instant.now()));
        verify(userRepository).save(user);
    }

    @Test
    void generatePasswordResetToken_shouldGenerateAndSaveToken() {
        User user = createUser();
        assertNull(user.getPasswordResetToken());

        authService.generatePasswordResetToken(user);

        assertNotNull(user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetTokenExpiration());
        assertTrue(user.getPasswordResetTokenExpiration().isAfter(Instant.now()));
        verify(userRepository).save(user);
    }

    @Test
    void resetUserPassword_shouldEncodeAndClearTokens() {
        User user = createUser();
        user.setPasswordResetToken("reset-token");
        user.setPasswordResetTokenExpiration(Instant.now().plusSeconds(300));

        String newPassword = "newSecurePassword123";

        authService.resetUserPassword(user, newPassword);

        assertNotNull(user.getPassword());
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetTokenExpiration());

        verify(userRepository).save(user);
    }
    
}

