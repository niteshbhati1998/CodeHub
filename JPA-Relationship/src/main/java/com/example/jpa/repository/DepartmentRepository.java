package com.example.jpa.repository;

import com.example.jpa.entity.o2m.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}