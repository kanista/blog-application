package com.example.blog.controller;

import com.example.blog.dto.CommonApiResponse;
import com.example.blog.dto.PostRequestDto;
import com.example.blog.entities.Post;
import com.example.blog.service.PostService;
import com.example.blog.util.JwtUtil;
import exception.GlobalExceptionHandler;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    private PostController(PostService postService, JwtUtil jwtUtil) {
        this.postService = postService;
        this.jwtUtil = jwtUtil;
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private String validateTokenAndGetEmail(HttpServletRequest request) {
        System.out.println("validateTokenAndGetEmail called");  // Add this to verify
        String token = request.getHeader("Authorization");
        logger.info("Authorization Header: {}", token);  // Check if token is properly sent

        if (token == null || !token.startsWith("Bearer ")) {
            throw new JwtException("Invalid or missing token.");
        }
        String email = jwtUtil.extractUsername(token.substring(7));  // Extract email
        logger.info("Extracted email: {}", email);  // Ensure this prints correctly

        return email;
    }


    @PostMapping("/post")
    public ResponseEntity<CommonApiResponse> createPost(@RequestBody PostRequestDto postRequestDto, HttpServletRequest request) {
        String email = validateTokenAndGetEmail(request);
        System.out.println("Email retrieved: " + email);

        try {

            Post createdPost = postService.createPost(postRequestDto, email);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CommonApiResponse<>(HttpStatus.CREATED.value(), "Post created successfully", createdPost));

        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }


}
