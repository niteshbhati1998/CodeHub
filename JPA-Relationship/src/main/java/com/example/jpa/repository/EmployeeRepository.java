package com.example.jpa.repository;

import com.example.jpa.entity.o2m.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}