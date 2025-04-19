package com.grimeyy.backend.user.data;

import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.grimeyy.backend.security.JwtUtil;

import jakarta.transaction.Transactional;
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
    	String email = extractEmail(authHeader);

        UserData user = userDataService.getUserByEmail(email);
        return userDataMapper.toDto(user);
    }
    
    @Transactional
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UserDataDto dto, @RequestHeader("Authorization") String authHeader) {
    	String email = extractEmail(authHeader);
        UserData updatedUser = userDataService.updateUserData(email, dto);
        return ResponseEntity.ok(userDataMapper.toDto(updatedUser));
    }


    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) throws IOException {
    	String email = extractEmail(authHeader);

        UserData user = userDataService.getUserByEmail(email);
        userDataService.uploadAvatar(user, file);
        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/avatar", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> getAvatar(@RequestHeader("Authorization") String authHeader) throws IOException {
        String email = extractEmail(authHeader);

        UserData user = userDataService.getUserByEmail(email);
        byte[] avatar = userDataService.getAvatar(user.getId());

        return ResponseEntity.ok()
        	.contentType(MediaType.valueOf(user.getAvatarContentType()))
            .body(avatar);
    }
    
    private String extractEmail(String authHeader) {
        return jwtUtil.extractEmail(authHeader.substring(7));
    }
}

