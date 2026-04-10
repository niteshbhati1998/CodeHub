package com.example.employeedetails.controller;

import com.example.employeedetails.entity.Department;
import com.example.employeedetails.model.DepartmentDTO;
import com.example.employeedetails.service.EmployeeDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
public class EmployeeDetailsController {

    @Autowired
    private EmployeeDetailsService employeeDetailsService;

    @GetMapping("/get")
    public ResponseEntity<?> getDepartment(@RequestBody Department department) {
        DepartmentDTO dept = employeeDetailsService.getDepartmentInfo(department);
        System.out.println(dept);
        return new ResponseEntity<>(dept, HttpStatus.OK);
    }
}