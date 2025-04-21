package com.grimeyy.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
	@NotBlank(message = "Email should not be empty!")
    @Email(message = "Email address is not valid!")
    private String email;

    @NotBlank(message = "Password should not be empty!")
    @Size(min = 6, message = "Password has to be at least 6 characters long!")
    private String password;
}
