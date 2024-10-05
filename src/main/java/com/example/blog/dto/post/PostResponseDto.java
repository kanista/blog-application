package com.example.blog.dto.post;

import com.example.blog.dto.user.UserDto;
import com.example.blog.dto.comment.CommentDto;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String body;
    private String status;
    private UserDto user;
    private List<CommentDto> comment;

}
