package com.grimeyy.backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toEntity_shouldMapDtoToEntityCorrectly() {
    	UserDto dto = new UserDto("test@example.com", "securePassword");

        User user = userMapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getPassword(), user.getPassword());
        assertFalse(user.isEmailConfirmed());
        assertNotNull(user.getEmailTokenExpiration());
    }

    @Test
    void toDto_shouldMapEntityToDtoCorrectly() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("securePassword");

        UserDto dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getPassword(), dto.getPassword());
    }

    @Test
    void toDto_shouldReturnNull_whenInputIsNull() {
        assertNull(userMapper.toDto(null));
    }

    @Test
    void toEntity_shouldReturnNull_whenInputIsNull() {
        assertNull(userMapper.toEntity(null));
    }
}