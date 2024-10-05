package com.example.blog.dto.user;

import com.example.blog.entities.Role;
import com.example.blog.entities.User;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Role role;

    // Constructor to convert User entity to UserDto
    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

}
