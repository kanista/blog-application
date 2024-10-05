package com.example.blog.controller;

import com.example.blog.dto.CommonApiResponse;
import com.example.blog.dto.user.UserDto;
import com.example.blog.dto.user.UserRequestDto;
import com.example.blog.service.UserService;
import com.example.blog.util.JwtUtil;
import exception.GlobalExceptionHandler;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
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


    @PatchMapping("/update")
    public ResponseEntity<CommonApiResponse<UserDto>> updateUserProfile(@RequestBody UserRequestDto userRequestDto, HttpServletRequest request) {
        String email = validateTokenAndGetEmail(request);

        try {
            UserDto updatedUser = userService.updateUserProfile(email, userRequestDto);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Profile updated successfully", updatedUser));
        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        }catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }


}