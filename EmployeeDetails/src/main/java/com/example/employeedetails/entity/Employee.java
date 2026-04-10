package com.example.employeedetails.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee", schema = "staging")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "emp_id")
    private Long id;

    @Column(name = "emp_name")
    private String empName;

    @ManyToOne
    @JoinColumn(name = "dept_id_fk", nullable = false)
    @JsonIgnore
    private Department department;

    public Employee(Long id, String empName) {
        this.id = id;
        this.empName = empName;
    }
}
