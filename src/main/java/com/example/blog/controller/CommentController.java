package com.example.blog.controller;

import com.example.blog.dto.comment.CommentRequestDto;
import com.example.blog.dto.CommonApiResponse;
import com.example.blog.entities.Comment;
import com.example.blog.service.CommentService;
import com.example.blog.util.JwtUtil;
import exception.GlobalExceptionHandler;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    public CommentController(CommentService commentService,JwtUtil jwtUtil){
        this.commentService = commentService;
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

    // Create a comment for a post
    @PostMapping("/post/{postId}")
    public ResponseEntity<CommonApiResponse<Comment>> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDto commentRequestDto,
            HttpServletRequest request) {

        try {
            String email = validateTokenAndGetEmail(request);
            Comment comment = commentService.createComment(commentRequestDto, email, postId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CommonApiResponse<>(HttpStatus.CREATED.value(), "Comment created successfully", comment));
        } catch (GlobalExceptionHandler.PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "Post not found", null));
        }catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    // Edit an existing comment
    @PutMapping("/{commentId}")
    public ResponseEntity<CommonApiResponse<Comment>> editComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto commentRequestDto,
            HttpServletRequest request) {

        try {
            String email = validateTokenAndGetEmail(request);
            Comment updatedComment = commentService.editComment(commentId, commentRequestDto, email);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Comment updated successfully", updatedComment));
        } catch (GlobalExceptionHandler.CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "Comment not found", null));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonApiResponse<String>> deleteComment(
            @PathVariable Long commentId, HttpServletRequest request) {

        try {
            String email = validateTokenAndGetEmail(request);
            commentService.deleteComment(commentId, email);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Comment deleted successfully", null));
        } catch (GlobalExceptionHandler.CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "Comment not found", null));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

    // Get all comments for a specific post
    @GetMapping("/post/{postId}")
    public ResponseEntity<CommonApiResponse<List<Comment>>> getCommentsByPostId(@PathVariable Long postId) {
        try {
            List<Comment> comments = commentService.getCommentsByPostId(postId);
            return ResponseEntity.ok(new CommonApiResponse<>(HttpStatus.OK.value(), "Comments retrieved successfully", comments));
        } catch (GlobalExceptionHandler.PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(HttpStatus.NOT_FOUND.value(), "Post not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred", null));
        }
    }

}