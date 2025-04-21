package com.grimeyy.backend.user.data;

import org.springframework.stereotype.Component;

@Component
public class UserDataMapper {

    public UserDataDto toDto(UserData user) {
        return new UserDataDto(user.getName(), user.getAddress(), user.getPhoneNumber());
    }

    public UserData toEntity(UserDataDto dto) {
        UserData user = new UserData();
        user.setName(dto.getName());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        return user;
    }
}
