package com.example.test.controller;

import com.example.test.entity.User;
import com.example.test.model.Response;
import com.example.test.service.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class MyController {

    @Autowired
    private MyService myService;

    @GetMapping("/save")
    public ResponseEntity<Response> saveUser(@RequestBody User user) {
        Response response = new Response();
        try {
            User savedUser = myService.saveUser(user);
            response.setMessage("User saved successfully");
            response.setPayload(savedUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception ex) {
            response.setMessage("Failed to save user");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Response> getUser(@PathVariable long id) {
        Response response = new Response();
        try {
            User user = myService.getUser(id);
            response.setMessage("User details fetched successfully");
            response.setPayload(user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception ex) {
            response.setMessage("Failed to fetch user details");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteUser(@PathVariable long id) {
        Response response = new Response();
        try {
            myService.deleteUser(id);
            response.setMessage("User details deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception ex) {
            response.setMessage("Failed to delete user details");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
