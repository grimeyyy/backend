package com.grimeyy.backend.auth.dto;

import lombok.*;

@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}
