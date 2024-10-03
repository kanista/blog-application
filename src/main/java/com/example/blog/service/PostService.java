package com.example.blog.service;

import com.example.blog.dto.PostRequestDto;
import com.example.blog.entities.Post;
import com.example.blog.entities.User;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import exception.GlobalExceptionHandler;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Post createPost(PostRequestDto postRequestDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        Post post = new Post();
        post.setTitle(postRequestDto.getTitle());
        post.setBody(postRequestDto.getBody());
        post.setStatus(postRequestDto.getStatus());
        post.setUser(user);

        return postRepository.save(post);
    }

}
