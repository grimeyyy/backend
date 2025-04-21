package com.grimeyy.backend.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimeyy.backend.exception.DuplicateEmailException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final String email = "test@example.com";
    private final UserDto userDto = new UserDto(email, "password");

    @Test
    void createUser_success() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void createUser_shouldFail_whenEmailIsMissing() throws Exception {
        UserDto invalidUser = new UserDto("", "securePassword");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldFail_whenEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new DuplicateEmailException("Email already exists"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email already exists")));
    }

    @Test
    void getUsers_shouldReturnList() throws Exception {
        List<UserDto> users = List.of(new UserDto("a@mail.com", "pass"));
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("a@mail.com"));
    }

    @Test
    void getUsers_shouldReturnEmptyList_whenNoUsersExist() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteUser_shouldReturnNoContent_whenUserExistsByEmail() throws Exception {
        when(userService.deleteUserByEmail(email))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/api/users").param("email", email))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExistByEmail() throws Exception {
        when(userService.deleteUserByEmail(email))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(delete("/api/users").param("email", email))
                .andExpect(status().isNotFound());
    }
}

