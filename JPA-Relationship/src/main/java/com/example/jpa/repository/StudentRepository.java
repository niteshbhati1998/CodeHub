package com.example.jpa.repository;

import com.example.jpa.entity.m2m.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}