package com.example.blog.repository;

import com.example.blog.entities.Post;
import com.example.blog.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserId(Long user_id); // Implement this query method
    // Find posts by user ID and status
    List<Post> findByUserIdAndStatus(Long userId, Status status);
}
