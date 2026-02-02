package com.example.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course", schema = "staging")
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String courseName;
}
