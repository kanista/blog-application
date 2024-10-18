package com.example.blog.service;

import com.example.blog.dto.comment.CommentDto;
import com.example.blog.dto.post.PostRequestDto;
import com.example.blog.dto.post.PostResponseDto;
import com.example.blog.dto.user.UserDto;
import com.example.blog.entities.Post;
import com.example.blog.entities.Status;
import com.example.blog.entities.User;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import exception.GlobalExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    public PostService(PostRepository postRepository, UserRepository userRepository, ImageUploadService imageUploadService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.imageUploadService = imageUploadService;
    }

    public Post createPost(PostRequestDto postRequestDto, String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        Post post = new Post();
        post.setTitle(postRequestDto.getTitle());
        post.setBody(postRequestDto.getBody());
        post.setStatus(postRequestDto.getStatus());
        post.setUser(user);
        post.setImageUrl(imageUrl);

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
        // Map User to UserDto
        User user = post.getUser();
        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());

        // Map Comments to CommentDto List
        List<CommentDto> commentDtos = post.getComments()
                .stream()
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getBody(),
                        comment.getCreatedAt(),
                        new UserDto(comment.getUser().getId(), comment.getUser().getName(), comment.getUser().getEmail(), comment.getUser().getRole())
                ))
                .collect(Collectors.toList());

        // Return the PostResponseDto with comments
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getStatus().toString(),
                post.getImageUrl(),
                userDto,
                commentDtos  // Include the list of comment DTOs
        );
    }


    // Delete a post by ID and email (only if the post belongs to the user)
    public void deletePost(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GlobalExceptionHandler.PostNotFoundException("Post not found"));

        // Ensure the post belongs to the user
        if (!post.getUser().getId().equals(user.getId())) {
            throw new GlobalExceptionHandler.UnauthorizedAccessException("You are not allowed to delete this post.");
        }

        postRepository.delete(post);
    }

    // Update a post by ID and email (only if the post belongs to the user)
    public Post updatePost(Long postId, PostRequestDto postRequestDto, String email, MultipartFile image) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GlobalExceptionHandler.PostNotFoundException("Post not found"));

        // Ensure the post belongs to the user
        if (!post.getUser().getId().equals(user.getId())) {
            throw new GlobalExceptionHandler.UnauthorizedAccessException("You are not allowed to update this post.");
        }

        // Update the post details
        post.setTitle(postRequestDto.getTitle());
        post.setBody(postRequestDto.getBody());
        post.setStatus(postRequestDto.getStatus());

        // Handle image upload if present
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadService.uploadImage(image);
            post.setImageUrl(imageUrl);  // Update the image URL
        }

        return postRepository.save(post);
    }


    // Get posts by status
    public List<Post> getPostsByStatus(String email,Status status) {
        // Find the user by email first
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("User not found"));

        // Now find posts by the user's ID and status
        return postRepository.findByUserIdAndStatus(user.getId(), status);
    }

}
