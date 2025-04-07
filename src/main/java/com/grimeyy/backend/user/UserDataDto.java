package com.grimeyy.backend.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDataDto {
    private String name;
    private String address;
    private String phoneNumber;
}
