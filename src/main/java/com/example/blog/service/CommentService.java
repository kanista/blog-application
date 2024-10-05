package com.example.blog.service;

import com.example.blog.dto.comment.CommentRequestDto;
import com.example.blog.entities.Comment;
import com.example.blog.entities.Post;
import com.example.blog.entities.User;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import exception.GlobalExceptionHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // Create a new comment
    public Comment createComment(CommentRequestDto commentRequestDto, String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GlobalExceptionHandler.PostNotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setBody(commentRequestDto.getBody());
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    // Edit an existing comment
    public Comment editComment(Long commentId, CommentRequestDto commentRequestDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        Comment comment = commentRepository.findByIdAndUserId(commentId, user.getId())
                .orElseThrow(() -> new GlobalExceptionHandler.CommentNotFoundException("Comment not found"));

        comment.setBody(commentRequestDto.getBody());
        return commentRepository.save(comment);
    }

    // Delete a comment
    public void deleteComment(Long commentId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        Comment comment = commentRepository.findByIdAndUserId(commentId, user.getId())
                .orElseThrow(() -> new GlobalExceptionHandler.CommentNotFoundException("Comment not found"));

        commentRepository.delete(comment);
    }

    // Retrieve all comments for a specific post
    public List<Comment> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GlobalExceptionHandler.PostNotFoundException("Post not found"));

        return commentRepository.findByPostId(postId);
    }
}
