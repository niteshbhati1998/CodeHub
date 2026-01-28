package com.example.test.service;

import com.example.test.entity.User;
import com.example.test.repository.MyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
public class MyService {

    @Autowired
    private MyRepository myRepository;

    public User getUserDetails(long id) {
        Optional<User> user = myRepository.findById(id);
        return user.orElseThrow(() -> new RuntimeException("Invalid user ID"));
    }

    public void deleteUserDetails(long id) {
        boolean isValid = validateId(id);
        if (isValid) {
            myRepository.deleteById(id);
        } else {
            throw new RuntimeException("Invalid user ID");
        }
    }

    private boolean validateId(long id) {
        return Stream.of(id).anyMatch(n->n<10);
    }
}
