package com.grimeyy.backend.user;

import org.springframework.web.bind.annotation.*;
import lombok.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDataController {
    
    private final UserDataService userDataService;

    @PostMapping("/sign-up")
    public UserData registerUser(@RequestBody UserData userData) {
        return userDataService.saveUserData(userData);
    }
}
