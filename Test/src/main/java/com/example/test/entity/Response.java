package com.example.test.entity;

import lombok.Data;

@Data
public class Response {
    private String message;
    private User payload;
}
