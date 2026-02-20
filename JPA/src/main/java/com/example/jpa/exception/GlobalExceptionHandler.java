package com.example.jpa.exception;

import com.example.jpa.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicantNotFoundException.class)
    public ResponseEntity<Response<?>> handleApplicantNotFoundException(ApplicantNotFoundException ex) {
        logger.error("Exception occured: ", ex);
        Response<?> response = new Response<>("failure", "No such Applicant data exists", null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleException(Exception ex) {
        logger.error("Exception occured: ", ex);
        Response<?> response = new Response<>("failure", "Internal Server Error", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.error("Exception occured: ", ex);
        Map<Object,Object> fieldsMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(n->{
            fieldsMap.put(n.getField(), n.getDefaultMessage());
        });
        Response<?> response = new Response<>("failure", fieldsMap, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}