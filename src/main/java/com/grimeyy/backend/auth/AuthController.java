
package com.grimeyy.backend.auth;


import com.grimeyy.backend.security.JwtUtil;
import com.grimeyy.backend.user.User;
import com.grimeyy.backend.user.UserRepository;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user.isPresent() && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            String token = jwtUtil.generateToken(user.get().getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User newUser = new User(null, request.getUsername(), passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }
}
