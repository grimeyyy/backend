package com.grimeyy.backend.user;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_data")
public class UserData extends User {

    private String name;
    private String address;
    private String phoneNumber;

    @Lob
    @Column(name = "avatar", columnDefinition = "BYTEA")
    private byte[] avatar;

    @Column(name = "avatar_content_type")
    private String avatarContentType;
}
