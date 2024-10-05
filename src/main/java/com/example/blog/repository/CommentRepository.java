package com.example.blog.repository;

import com.example.blog.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId); // Get all comments by post
    Optional<Comment> findByIdAndUserId(Long commentId, Long userId); // Find a specific comment for a user
}
