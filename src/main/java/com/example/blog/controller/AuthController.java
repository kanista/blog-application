package com.example.blog.controller;

import com.example.blog.dto.CommonApiResponse;
import com.example.blog.dto.RegisterRequestDto;
import com.example.blog.dto.UserDto;
import com.example.blog.service.AuthService;
import exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonApiResponse<UserDto>> registerUser(@RequestBody RegisterRequestDto request) {
        System.out.println("Received registration request: " + request);

        if (authService.checkEmailExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Email already exists", null));
        }

        try {
            UserDto registeredUser = authService.registerUser(request);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "User registered successfully", registeredUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<CommonApiResponse> loginUser(@RequestBody @Valid RegisterRequestDto request) {
        System.out.println("Received registration request: " + request);
        try {
            String jwt = authService.loginUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Login successful", jwt));
        } catch (GlobalExceptionHandler.InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password", null));
        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error: " + e.getMessage(), null));
        }
    }

}
