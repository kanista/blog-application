package com.example.blog.controller;

import com.example.blog.dto.CommonApiResponse;
import com.example.blog.dto.user.RegisterRequestDto;
import com.example.blog.dto.user.UserDto;
import com.example.blog.service.AdminService;
import com.example.blog.util.JwtUtil;
import exception.GlobalExceptionHandler;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AdminService adminService , JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    private String validateTokenAndGetEmail(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new JwtException("Invalid or missing token.");
        }

        String email = jwtUtil.extractUsername(token.substring(7));  // Extract email

        if (email == null) {
            throw new JwtException("Token validation failed, email not found.");
        }

        return email;
    }


    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<UserDto>> registerAdmin(@RequestBody RegisterRequestDto request) {

        try {
            UserDto newAdmin = adminService.registerAdmin(request);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Admin registered successfully", newAdmin));
        } catch (GlobalExceptionHandler.EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Email already exists", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<String>> deleteUserById(@PathVariable Long id, HttpServletRequest request) {
        try {
            String email = validateTokenAndGetEmail(request);

            if (!adminService.isAdmin(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new CommonApiResponse<>(HttpStatus.FORBIDDEN.value(), "Access denied. Admin role required.", null));
            }

            adminService.deleteUserById(id);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", null));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<List<UserDto>>> getAllUsers(HttpServletRequest request) {
        try {
            String email = validateTokenAndGetEmail(request);

            if (!adminService.isAdmin(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new CommonApiResponse<>(HttpStatus.FORBIDDEN.value(), "Access denied. Admin role required.", null));
            }

            List<UserDto> allUsers = adminService.getAllUsers();
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "All users retrieved successfully", allUsers));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }


}

