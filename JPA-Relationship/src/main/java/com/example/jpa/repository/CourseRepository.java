package com.example.jpa.repository;

import com.example.jpa.entity.m2m.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}