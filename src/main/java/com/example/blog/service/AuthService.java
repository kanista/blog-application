package com.example.blog.service;

import com.example.blog.dto.user.RegisterRequestDto;
import com.example.blog.dto.user.UserDto;
import com.example.blog.entities.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.JwtUtil;
import exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Constructor injection
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Method to check if email already exists
    public boolean checkEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public UserDto registerUser(@Valid RegisterRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new GlobalExceptionHandler.PasswordMismatchException("Passwords do not match");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(request.getRole());
        userRepository.save(user);
        return new UserDto(user); // Convert to DTO
    }


    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GlobalExceptionHandler.InvalidCredentialsException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getName(), user.getEmail(), user.getRole()); // Generate JWT token for the user
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Load user from database by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert your User entity to UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name()) // Assuming Role is an enum
                .build();
    }
}
