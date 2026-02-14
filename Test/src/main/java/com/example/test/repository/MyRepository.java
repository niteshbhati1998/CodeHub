package com.example.test.repository;

import com.example.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyRepository extends JpaRepository<User, Long> {
}
