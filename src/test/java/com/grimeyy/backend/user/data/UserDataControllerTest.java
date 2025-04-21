package com.grimeyy.backend.user.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimeyy.backend.security.jwt.JwtUtil;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserDataControllerTest {

	 @Autowired
	    private MockMvc mockMvc;

	    @MockitoBean
	    private UserDataService userDataService;

	    @MockitoBean
	    private UserDataMapper userDataMapper;

	    @MockitoBean
	    private JwtUtil jwtUtil;

	    private final String token = "Bearer faketoken123";
	    private final String email = "test@example.com";

	    private final UserData userData = new UserData();
	    private final UserDataDto dto = new UserDataDto();

	    @BeforeEach
	    void setup() {
	        when(jwtUtil.extractEmail(anyString())).thenReturn(email);
	        when(userDataService.getUserByEmail(email)).thenReturn(userData);
	    }

	    @Test
	    void getProfile_shouldReturnUserData() throws Exception {
	        when(userDataMapper.toDto(userData)).thenReturn(dto);

	        mockMvc.perform(get("/api/users/profile").header("Authorization", token))
	                .andExpect(status().isOk());
	    }

	    @Test
	    void updateProfile_shouldUpdateAndReturnUserData() throws Exception {
	        when(userDataService.updateUserData(eq(email), any(UserDataDto.class))).thenReturn(userData);
	        when(userDataMapper.toDto(userData)).thenReturn(dto);

	        mockMvc.perform(put("/api/users/profile/update")
	                .header("Authorization", token)
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(new ObjectMapper().writeValueAsString(dto)))
	                .andExpect(status().isOk());
	    }

	    @Test
	    void uploadAvatar_shouldSucceed() throws Exception {
	        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "fake".getBytes());

	        mockMvc.perform(multipart("/api/users/profile/avatar")
	                .file(file)
	                .header("Authorization", token))
	                .andExpect(status().isOk());
	    }

	    @Test
	    void getAvatar_shouldReturnImage() throws Exception {
	        userData.setAvatarContentType(MediaType.IMAGE_PNG_VALUE);
	        byte[] image = "fakeImageBytes".getBytes();

	        when(userDataService.getAvatar(any())).thenReturn(image);

	        mockMvc.perform(get("/api/users/profile/avatar")
	                .header("Authorization", token))
	                .andExpect(status().isOk())
	                .andExpect(content().contentType(MediaType.IMAGE_PNG));
	    }
	    
	    @Test
	    void deleteProfile_shouldDeleteUserDataAndReturnNoContent() throws Exception {
	        mockMvc.perform(delete("/api/users/profile")
	                        .header("Authorization", token))
	                .andExpect(status().isNoContent());

	        verify(userDataService, times(1)).deleteUserData(email);
	    }
}
