package com.grimeyy.backend.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@MockitoBean
	UserRepository userRepository;
	
	private final String email = "test@example.com";
    private final User user = new User(1L, email, "password", false, null, Instant.now(), null, null, null, null);

	
	@Test
	void createUser_success() throws Exception {
	    User userToCreate = new User();
	    userToCreate.setEmail("test@example.com");
	    userToCreate.setPassword("securePassword");

	    when(userRepository.save(any(User.class))).thenReturn(userToCreate);

	    mockMvc.perform(post("/api/users")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(userToCreate)))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.email").value("test@example.com"));
	}
	
	@Test
	void createUser_shouldFail_whenEmailIsMissing() throws Exception {
	    User invalidUser = new User();
	    invalidUser.setPassword("securePassword");

	    mockMvc.perform(post("/api/users")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(invalidUser)))
	            .andExpect(status().isBadRequest());
	}
	
	@Test
	void createUser_shouldFail_whenEmailAlreadyExists() throws Exception {
	    when(userRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

	    User userToCreate = new User();
	    userToCreate.setEmail(email);
	    userToCreate.setPassword("somepass");

	    mockMvc.perform(post("/api/users")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(userToCreate)))
	            .andExpect(status().isBadRequest());
	}
	
	@Test
    void getUsers_shouldReturnList() throws Exception {
        List<User> users = List.of(new User(1L, "a@mail.com", "pass", true, null, null, null, null, null, null));
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("a@mail.com"));
    }
	
	@Test
	void getUsers_shouldReturnEmptyList_whenNoUsersExist() throws Exception {
	    when(userRepository.findAll()).thenReturn(Collections.emptyList());

	    mockMvc.perform(get("/api/users"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.length()").value(0));
	}
	
	@Test
    void deleteUser_shouldReturnNoContent_whenUserExistsByEmail() throws Exception {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        mockMvc.perform(delete("/api/users").param("email", email))
                .andExpect(status().isNoContent());

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExistByEmail() throws Exception {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users").param("email", email))
                .andExpect(status().isNotFound());

        verify(userRepository, never()).delete(any());
    }
}
