package com.example.blog.controller;

import com.example.blog.dto.CommonApiResponse;
import com.example.blog.dto.PostRequestDto;
import com.example.blog.dto.PostResponseDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    private PostController(PostService postService, JwtUtil jwtUtil) {
        this.postService = postService;
        this.jwtUtil = jwtUtil;
    }

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

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



    @PostMapping("/post")
    public ResponseEntity<CommonApiResponse> createPost(@RequestBody PostRequestDto postRequestDto, HttpServletRequest request) {


        try {

            String email = validateTokenAndGetEmail(request);
            System.out.println("Email retrieved: " + email);
            Post createdPost = postService.createPost(postRequestDto, email);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CommonApiResponse<>(HttpStatus.CREATED.value(), "Post created successfully", createdPost));

        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        }catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<CommonApiResponse<List<PostResponseDto>>> getAllPosts(HttpServletRequest request) {
        try {
            String email = validateTokenAndGetEmail(request);  // Validate the token and get the email

            List<PostResponseDto> posts = postService.getAllPosts();  // Get all posts
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Posts retrieved successfully", posts));

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while retrieving posts", null));
        }
    }

    @GetMapping("/posts/user")
    public ResponseEntity<CommonApiResponse<List<Post>>> getPostsByUser(HttpServletRequest request) {
        try {
            String email = validateTokenAndGetEmail(request);  // Validate the token and get the email

            List<Post> userPosts = postService.getPostsByEmail(email);  // Get user-specific posts
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "User posts retrieved successfully", userPosts));

        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while retrieving user posts", null));
        }
    }




}
