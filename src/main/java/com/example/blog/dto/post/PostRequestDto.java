package com.example.blog.dto.post;

import com.example.blog.entities.Status;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDto {
    private String title;
    private String body;
    private Status status;

}
