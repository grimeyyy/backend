package com.grimeyy.backend.user;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.grimeyy.backend.user.data.UserData;
import com.grimeyy.backend.user.data.UserDataRepository;
import com.grimeyy.backend.user.data.UserDataService;

class UserDataServiceTest {

    @Mock
    private UserDataRepository userDataRepository;

    @InjectMocks
    private UserDataService userDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); 
    }

    @Test
    void testSaveUserData() {
        UserData userData = new UserData();
        userData.setEmail("test@example.com");
        userData.setPassword("password123");
        userData.setName("Max Mustermann");

        when(userDataRepository.save(any(UserData.class))).thenReturn(userData);

        UserData savedUser = userDataService.saveUserData(userData);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        verify(userDataRepository, times(1)).save(any(UserData.class));
    }
}
