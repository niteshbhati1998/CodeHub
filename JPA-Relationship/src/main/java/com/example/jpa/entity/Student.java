package com.example.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student", schema = "staging")
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;
}
