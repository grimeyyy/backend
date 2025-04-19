package com.grimeyy.backend.user;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailConfirmed = false;
    
    private String emailToken;
    
    @Column(nullable = false)
    private Instant emailTokenExpiration;
    
    private String passwordResetToken;

    private Instant passwordResetTokenExpiration;

    private String refreshToken;
    
    private Instant refreshTokenExpiration;

}
