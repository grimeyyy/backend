package com.grimeyy.backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Test
    void getAllUsers_shouldReturnUserList() {
        List<User> users = List.of(new User(1L, "test@example.com", "password", false, null, Instant.now(), null, null, null, null));
        UserDto userDto = new UserDto("test@example.com", "password");

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
    }
    
    @Test
    void createUser_shouldReturnCreatedUser() {
        UserDto userDto = new UserDto("test@example.com", "securePassword");
        User user = new User(null, "test@example.com", "securePassword", false, null, Instant.now(), null, null, null, null);
        User savedUser = new User(1L, "test@example.com", "securePassword", false, null, Instant.now(), null, null, null, null);
        UserDto createdUserDto = new UserDto("test@example.com", "securePassword");

        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(createdUserDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("securePassword", result.getPassword());
    }
    
    @Test
    void deleteUser_shouldReturnNoContent_whenUserExists() {
        String email = "test@example.com";
        User user = new User(1L, email, "password", false, null, Instant.now(), null, null, null, null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = userService.deleteUserByEmail(email);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = userService.deleteUserByEmail(email);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).delete(any());
    }


}
