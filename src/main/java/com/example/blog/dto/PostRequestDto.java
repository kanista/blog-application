package com.example.blog.dto;

import com.example.blog.entities.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDto {
    private String title;
    private String body;
    private Status status;

}
