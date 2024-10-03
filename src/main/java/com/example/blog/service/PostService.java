package com.example.blog.service;

import com.example.blog.dto.PostRequestDto;
import com.example.blog.dto.PostResponseDto;
import com.example.blog.dto.UserDto;
import com.example.blog.entities.Post;
import com.example.blog.entities.User;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import exception.GlobalExceptionHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    // Fetch all posts
    public List<PostResponseDto> getAllPosts() {
//        return postRepository.findAll(); // This retrieves all posts
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(this::mapToPostResponseDto).collect(Collectors.toList());
    }

    // Method to get posts by user email
    public List<Post> getPostsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        return postRepository.findByUserId(user.getId()); // Fetch posts by user ID
    }

    private PostResponseDto mapToPostResponseDto(Post post) {
        User user = post.getUser();
        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(),user.getRole());

        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getStatus().toString(),
                userDto
        );
    }


}
