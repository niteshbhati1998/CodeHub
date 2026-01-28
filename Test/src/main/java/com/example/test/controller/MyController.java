package com.example.test.controller;

import com.example.test.entity.Response;
import com.example.test.entity.User;
import com.example.test.service.MyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class MyController {

    @Autowired
    private MyService myService;

    Logger logger = LoggerFactory.getLogger(MyController.class);

    @GetMapping("/getUserDetails/{id}")
    public ResponseEntity<Response> getUsers(@PathVariable long id) {
        Response response = new Response();
        try {
            User user = myService.getUserDetails(id);
            response.setMessage("User details fetched successfully");
            response.setPayload(user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            response.setMessage("Failed to fetch user details");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteUserDetails/{id}")
    public ResponseEntity<Response> deleteUsers(@PathVariable long id) {
        Response response = new Response();
        try {
            myService.deleteUserDetails(id);
            response.setMessage("User details deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            response.setMessage("Failed to delete user details");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
