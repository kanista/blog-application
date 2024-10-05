package com.example.blog.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonApiResponse <T>{
    private int status;
    private String message;
    private T data;
}
