package com.grimeyy.backend.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@MockitoBean
	UserRepository userRepository;
	
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
    void getUsers_shouldReturnList() throws Exception {
        List<User> users = List.of(new User(1L, "a@mail.com", "pass", true, null, null, null, null, null, null));
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("a@mail.com"));
    }
}
