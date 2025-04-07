package com.grimeyy.backend.user;

import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.grimeyy.backend.security.JwtUtil;

import lombok.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserDataController {

    private final UserDataService userDataService;
    private final UserDataMapper userDataMapper;
    private final JwtUtil jwtUtil;

    @GetMapping
    public UserDataDto getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserData user = userDataService.getUserByEmail(email);
        return userDataMapper.toDto(user);
    }

    @PutMapping
    public UserDataDto updateProfile(@RequestBody UserDataDto dto, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserData user = userDataService.getUserByEmail(email);
        userDataMapper.updateFromDto(dto, user);
        userDataService.saveUserData(user);

        return userDataMapper.toDto(user);
    }
    
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody UserDataDto dto, @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        UserData updatedUser = userDataService.updateUserData(email, dto);
        return ResponseEntity.ok(updatedUser);
    }


    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) throws IOException {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserData user = userDataService.getUserByEmail(email);
        userDataService.uploadAvatar(user, file);
        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/avatar", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> getAvatar(@RequestHeader("Authorization") String authHeader) throws IOException {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserData user = userDataService.getUserByEmail(email);
        byte[] avatar = userDataService.getAvatar(user.getId());

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG) 
            .body(avatar);
    }
}

