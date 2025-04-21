package com.grimeyy.backend.user.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
public class UserDataServiceTest {

    @MockitoBean
    private UserDataRepository userDataRepository;

    @Autowired
    private UserDataService userDataService;

    private UserData createTestUser(Long id, String name, String email, String address, String phone, byte[] avatar) {
        UserData user = new UserData();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123");
        user.setAddress(address);
        user.setPhoneNumber(phone);
        user.setAvatar(avatar);
        return user;
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenUserExists() {
        String email = "test@example.com";
        UserData user = createTestUser(1L, "John Doe", email, "Some Address", "123-456-7890", null);

        when(userDataRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserData result = userDataService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getUserByEmail_shouldThrowException_whenUserDoesNotExist() {
        String email = "nonexistent@example.com";

        when(userDataRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userDataService.getUserByEmail(email));
        assertEquals("ERROR.USER_NOT_FOUND", exception.getMessage());
    }

    @Test
    void saveUserData_shouldSaveUser() {
        UserData userData = createTestUser(null, "Jane Doe", "jane@example.com", "456 Some Street", "987-654-3210", null);

        when(userDataRepository.save(userData)).thenReturn(userData);

        UserData result = userDataService.saveUserData(userData);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("jane@example.com", result.getEmail());
    }

    @Test
    void updateUserData_shouldUpdateUserData() {
        String email = "test@example.com";
        UserData existingUser = createTestUser(1L, "Old Name", email, "Old Address", "000-000-0000", null);
        UserDataDto dto = new UserDataDto("New Name", "New Address", "111-222-3333");

        when(userDataRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userDataRepository.save(existingUser)).thenReturn(existingUser);

        UserData updatedUser = userDataService.updateUserData(email, dto);

        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        assertEquals("New Address", updatedUser.getAddress());
        assertEquals("111-222-3333", updatedUser.getPhoneNumber());
    }

    @Test
    void uploadAvatar_shouldUpdateAvatar() throws IOException {
        String email = "test@example.com";
        byte[] avatarBytes = "avatarBytes".getBytes();
        UserData user = createTestUser(1L, "John Doe", email, "Some Address", "123-456-7890", null);
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", avatarBytes);

        when(userDataRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userDataRepository.save(user)).thenReturn(user);

        userDataService.uploadAvatar(user, file);

        assertArrayEquals(avatarBytes, user.getAvatar());
        verify(userDataRepository).save(user);
    }

    @Test
    void getAvatar_shouldReturnAvatar_whenUserExists() {
        Long userId = 1L;
        byte[] avatarBytes = "avatarBytes".getBytes();
        UserData user = createTestUser(userId, "John Doe", "test@example.com", "Some Address", "123-456-7890", avatarBytes);

        when(userDataRepository.findById(userId)).thenReturn(Optional.of(user));

        byte[] result = userDataService.getAvatar(userId);

        assertNotNull(result);
        assertArrayEquals(avatarBytes, result);
    }

    @Test
    void deleteUserData_shouldDeleteUser_whenUserExists() {
        String email = "test@example.com";
        UserData user = createTestUser(1L, "John Doe", email, "Some Address", "123-456-7890", null);

        when(userDataRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(userDataRepository).delete(user);

        userDataService.deleteUserData(email);

        verify(userDataRepository, times(1)).delete(user);
    }

    @Test
    void deleteUserData_shouldThrowException_whenUserDoesNotExist() {
        String email = "nonexistent@example.com";
        when(userDataRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userDataService.deleteUserData(email));
    }
}
