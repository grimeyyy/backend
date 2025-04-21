package com.grimeyy.backend.user;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
    	if (user == null) {
            return null;
        }
        return new UserDto(user.getEmail(), user.getPassword());
    }

    public User toEntity(UserDto dto) {
    	 if (dto == null) {
             return null;
         }
        return new User(null, dto.getEmail(), dto.getPassword(), false,
                null, Instant.now().plus(Duration.ofDays(1)), null, null, null, null);
    }
}

