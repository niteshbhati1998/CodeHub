package com.example.jpa.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private String message;
    private String description;
    private T payload;

    public Response(String message, T payload) {
        this.message = message;
        this.payload = payload;
    }
}
