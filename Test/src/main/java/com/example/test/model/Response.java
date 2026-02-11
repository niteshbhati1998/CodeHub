package com.example.test.model;

import com.example.test.entity.User;
import lombok.Data;

@Data
public class Response {
    private String message;
    private User payload;
}
