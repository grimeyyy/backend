package com.grimeyy.backend.user.data;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;


import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDataService {

    private final UserDataRepository userDataRepository;


    public UserData getUserByEmail(String email) {
        Optional<UserData> user = userDataRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("ERROR.USER_NOT_FOUND");
        }
        return user.get();
    }

    public UserData saveUserData(UserData userData) {
        return userDataRepository.save(userData);
    }
    
    public UserData updateUserData(String email, UserDataDto dto) {
        UserData user = getUserByEmail(email);
        user.setName(dto.getName());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        return userDataRepository.save(user);
    }

    @Transactional
    public void uploadAvatar(UserData user, MultipartFile file) throws IOException {
        byte[] avatarBytes = file.getBytes();
        user.setAvatar(avatarBytes);
        saveUserData(user);
    }

    public byte[] getAvatar(Long userId) {
        UserData user = userDataRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAvatar();
    }
}

