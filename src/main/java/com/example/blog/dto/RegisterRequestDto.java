package com.example.blog.dto;

import com.example.blog.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Email cannot be null or empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be null or empty")
    private String password;

    private String confirmPassword;
    private String name;
    private Role role;
}
