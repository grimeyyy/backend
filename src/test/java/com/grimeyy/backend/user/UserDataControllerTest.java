package com.grimeyy.backend.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

class UserDataControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserDataService userDataService;

    @InjectMocks
    private UserDataController userDataController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userDataController).build();
    }

    @Test
    void testRegisterUser() throws Exception {
        UserData userData = new UserData();
        userData.setEmail("test@example.com");
        userData.setPassword("password123");
        userData.setName("Max Mustermann");

        when(userDataService.saveUserData(any(UserData.class))).thenReturn(userData);

        mockMvc.perform(post("/api/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}

