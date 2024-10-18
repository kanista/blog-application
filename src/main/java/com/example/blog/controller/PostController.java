package com.example.blog.controller;

import com.example.blog.dto.CommonApiResponse;
import com.example.blog.dto.post.PostRequestDto;
import com.example.blog.dto.post.PostResponseDto;
import com.example.blog.entities.Post;
import com.example.blog.entities.Status;
import com.example.blog.service.ImageUploadService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;
    private final ImageUploadService imageUploadService;

    private PostController(PostService postService, JwtUtil jwtUtil, ImageUploadService imageUploadService) {
        this.postService = postService;
        this.jwtUtil = jwtUtil;
        this.imageUploadService = imageUploadService;
    }

//    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

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
    public ResponseEntity<CommonApiResponse> createPost(@RequestPart("post") PostRequestDto postRequestDto,
                                                        @RequestPart("image") MultipartFile image,
                                                        HttpServletRequest request) {

        try {
            String email = validateTokenAndGetEmail(request);
            String imageUrl = null;

            // Handle image upload if present
            if (!image.isEmpty()) {
                imageUrl = imageUploadService.uploadImage(image);
            }

            // Create the post with the image URL
            Post createdPost = postService.createPost(postRequestDto, email, imageUrl);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CommonApiResponse<>(HttpStatus.CREATED.value(), "Post created successfully", createdPost));

        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        } catch (JwtException e) {
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

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<CommonApiResponse<Void>> deletePost(@PathVariable Long postId, HttpServletRequest request) {

        try {
            String email = validateTokenAndGetEmail(request);  // Validate the token and get the email

            List<Post> userPosts = postService.getPostsByEmail(email);
            postService.deletePost(postId, email);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Post deleted successfully", null));
        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found.", null));
        } catch (GlobalExceptionHandler.PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "Post not found.", null));
        }catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while deleting the post.", null));
        }
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<CommonApiResponse<Post>> updatePost(@PathVariable Long postId,
                                                              @RequestPart("post") PostRequestDto postRequestDto,
                                                              @RequestPart(value = "image", required = false) MultipartFile image,
                                                              HttpServletRequest request) {

        try {
            String email = validateTokenAndGetEmail(request);
            System.out.println("Email retrieved: " + email);

            Post updatedPost = postService.updatePost(postId, postRequestDto, email, image);

            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Post updated successfully", updatedPost));
        } catch (GlobalExceptionHandler.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found.", null));
        } catch (GlobalExceptionHandler.PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "Post not found.", null));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while updating the post.", null));
        }
    }


    @GetMapping("/posts/filter")
    public ResponseEntity<CommonApiResponse<List<Post>>> getPostsByStatus(@RequestParam("status") Status status, HttpServletRequest request) {

        try {
            String email = validateTokenAndGetEmail(request);
            System.out.println("Email retrieved: " + email);
            // Call service to get posts by status
            List<Post> filteredPosts = postService.getPostsByStatus(email,status);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Posts filtered by status retrieved successfully", filteredPosts));
        }catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while retrieving filtered posts.", null));
        }
    }


}
