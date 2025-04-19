package com.grimeyy.backend.user.data;

import org.springframework.stereotype.Component;

@Component
public class UserDataMapper {
    public UserDataDto toDto(UserData user) {
        return new UserDataDto(user.getName(), user.getAddress(), user.getPhoneNumber());
    }

    public void updateFromDto(UserDataDto dto, UserData user) {
        user.setName(dto.getName());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
    }
}