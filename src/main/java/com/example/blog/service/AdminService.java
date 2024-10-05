package com.example.blog.service;

import com.example.blog.dto.user.RegisterRequestDto;
import com.example.blog.dto.user.UserDto;
import com.example.blog.entities.Role;
import com.example.blog.entities.User;
import com.example.blog.repository.UserRepository;
import exception.GlobalExceptionHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Method to check if email already exists
    public boolean checkEmailExists(String email) {

        return userRepository.findByEmail(email).isPresent();
    }

    public UserDto registerAdmin(RegisterRequestDto request) {

        if (checkEmailExists(request.getEmail())) {
            throw new GlobalExceptionHandler.EmailAlreadyExistsException("Email already exists");
        }

        User admin = new User();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMIN);

        User savedAdmin = userRepository.save(admin);
        return new UserDto(savedAdmin.getId(), savedAdmin.getName(), savedAdmin.getEmail(), savedAdmin.getRole());
    }

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        userRepository.delete(user);
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }

    public boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        return user.getRole().equals(Role.ADMIN);
    }


}
