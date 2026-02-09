package com.example.caching.model;

import lombok.Data;

@Data
public class Response<T> {

    private String message;
    private T payload;
}
